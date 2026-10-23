/**
 * Skill Assessment: Interactive Current & Target Skill Proficiency Rating
 */

let allSkills = [];
let allRoles = [];
let currentRoleId = 1;
let currentRole = null;
let currentCategory = 'All';
const userRatings = {};
const userNotes = {};

document.addEventListener('DOMContentLoaded', async () => {
  initUserProfile();
  await loadRolesCatalog();
  await loadAssessmentSkills();
  setupRoleChangeHandler();
  setupSubmitHandler();
});

function initUserProfile() {
  const saved = sessionStorage.getItem('skillgap_user');
  if (saved) {
    try {
      const u = JSON.parse(saved);
      if (u.fullName) {
        const badge = document.getElementById('navUserName');
        if (badge) badge.innerHTML = `<i class="fas fa-user-circle"></i> ${u.fullName}`;
      }
      if (u.targetRoleId) {
        currentRoleId = parseInt(u.targetRoleId, 10);
      }
    } catch (e) {}
  }
}

async function loadRolesCatalog() {
  try {
    const res = await API.get('/api/roles');
    if (res?.data && Array.isArray(res.data)) {
      allRoles = res.data;
      const select = document.getElementById('targetRoleSelect');
      if (select) {
        select.innerHTML = allRoles.map(r => `
          <option value="${r.id}" ${r.id === currentRoleId ? 'selected' : ''}>${r.title}</option>
        `).join('');
      }
      updateActiveRole();
    }
  } catch (err) {
    console.error('Could not load roles catalog:', err);
  }
}

function updateActiveRole() {
  currentRole = allRoles.find(r => r.id === currentRoleId) || (allRoles.length > 0 ? allRoles[0] : null);
  const summary = document.getElementById('targetGoalSummary');
  if (summary && currentRole) {
    summary.textContent = `Targeting: ${currentRole.title} (${currentRole.category})`;
  }
}

function setupRoleChangeHandler() {
  const select = document.getElementById('targetRoleSelect');
  if (!select) return;

  select.addEventListener('change', async () => {
    currentRoleId = parseInt(select.value, 10);
    updateActiveRole();

    // Update in sessionStorage and notify backend
    const saved = sessionStorage.getItem('skillgap_user');
    if (saved) {
      try {
        const u = JSON.parse(saved);
        u.targetRoleId = currentRoleId;
        u.targetRoleTitle = currentRole?.title || '';
        sessionStorage.setItem('skillgap_user', JSON.stringify(u));
      } catch (e) {}
    }

    try {
      await API.post('/api/roles/select', { roleId: currentRoleId });
    } catch (e) {}

    renderSkills();
  });
}

async function loadAssessmentSkills() {
  const container = document.getElementById('skillsContainer');
  if (!container) return;

  container.innerHTML = `
    <div style="text-align: center; padding: 3rem; color: var(--slate-500);">
      <i class="fas fa-spinner fa-spin" style="font-size: 2rem; color: #2563eb;"></i>
      <p style="margin-top: 1rem;">Loading skills catalog & industry benchmarks...</p>
    </div>
  `;

  try {
    const res = await API.get('/api/assessment/skills');
    if (!res || !res.data) throw new Error('Could not retrieve skills catalog');

    allSkills = res.data;

    // Initialize ratings from server
    allSkills.forEach(s => {
      if (s.currentRating !== undefined && s.currentRating !== null) {
        userRatings[s.id] = s.currentRating;
      }
    });

    renderCategories();
    renderSkills();
    updateCounter();
  } catch (err) {
    container.innerHTML = `
      <div style="text-align: center; padding: 2rem; color: var(--danger);">
        <i class="fas fa-exclamation-circle" style="font-size: 2rem;"></i>
        <p style="margin-top: 0.5rem;">Failed to load skills: ${err.message}</p>
      </div>
    `;
  }
}

function getRequiredSkillsMap() {
  const map = new Map();
  if (currentRole && currentRole.requiredSkills) {
    currentRole.requiredSkills.forEach(rs => {
      map.set(rs.skillId, rs);
    });
  }
  return map;
}

function renderCategories() {
  const tabsContainer = document.getElementById('categoryTabs');
  if (!tabsContainer) return;

  const rawCats = [...new Set(allSkills.map(s => s.category))];
  const categories = ['All', 'Target Role Skills', ...rawCats];

  tabsContainer.innerHTML = categories.map(cat => `
    <button class="tab-btn ${cat === currentCategory ? 'active' : ''}" data-category="${cat}">
      ${cat}
    </button>
  `).join('');

  tabsContainer.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      tabsContainer.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
      e.target.classList.add('active');
      currentCategory = e.target.getAttribute('data-category');
      renderSkills();
    });
  });
}

function renderSkills() {
  const container = document.getElementById('skillsContainer');
  if (!container) return;

  const reqMap = getRequiredSkillsMap();

  let filtered = allSkills;
  if (currentCategory === 'Target Role Skills') {
    filtered = allSkills.filter(s => reqMap.has(s.id));
  } else if (currentCategory !== 'All') {
    filtered = allSkills.filter(s => s.category === currentCategory);
  }

  if (filtered.length === 0) {
    container.innerHTML = `<p style="text-align: center; padding: 2rem; color: var(--slate-400);">No skills found in this category.</p>`;
    return;
  }

  container.innerHTML = filtered.map(skill => {
    const selectedLevel = userRatings[skill.id] !== undefined ? userRatings[skill.id] : 0;
    const req = reqMap.get(skill.id);
    const isTargetSkill = !!req;

    return `
      <div class="skill-card ${isTargetSkill ? 'is-target-role' : ''}" id="skill-card-${skill.id}">
        <div class="skill-meta">
          <h4>
            <i class="fas fa-check-circle" style="color: ${selectedLevel > 0 ? '#10b981' : '#cbd5e1'};"></i>
            ${skill.name}
          </h4>
          <div style="display: flex; gap: 0.5rem; flex-wrap: wrap; align-items: center; margin-top: 0.35rem;">
            <span class="badge badge-primary">${skill.category}</span>
            ${isTargetSkill ? `
              <span class="target-benchmark-indicator">
                <i class="fas fa-bullseye"></i> Benchmark: Level ${req.requiredProficiency} (${req.importance || 'High'})
              </span>
            ` : ''}
          </div>
          <p>${skill.description || 'Core technical competency.'}</p>
        </div>

        <div class="rating-control-wrap">
          <div style="font-size: 0.8rem; font-weight: 600; color: var(--slate-600); margin-bottom: 0.2rem;">
            Your Current Proficiency Level:
          </div>
          <div class="rating-pills">
            ${[0, 1, 2, 3, 4, 5].map(lvl => `
              <div class="rating-pill ${lvl === 0 ? 'pill-zero' : ''} ${selectedLevel === lvl ? 'selected' : ''}"
                   onclick="selectRating(${skill.id}, ${lvl})"
                   title="${lvl === 0 ? 'None / Not acquired' : 'Level ' + lvl}">
                ${lvl === 0 ? '0' : lvl}
              </div>
            `).join('')}
          </div>
          <div class="level-legend">
            <span>0: None</span>
            <span>1: Novice</span>
            <span>2: Beginner</span>
            <span>3: Competent</span>
            <span>4: Proficient</span>
            <span>5: Expert</span>
          </div>
          <input type="text"
                 class="skill-notes-input"
                 placeholder="Optional notes or project experience..."
                 value="${userNotes[skill.id] || ''}"
                 onchange="updateNote(${skill.id}, this.value)">
        </div>
      </div>
    `;
  }).join('');
}

window.selectRating = function(skillId, level) {
  userRatings[skillId] = level;
  renderSkills();
  updateCounter();
};

window.updateNote = function(skillId, note) {
  userNotes[skillId] = note;
};

function updateCounter() {
  const countSpan = document.getElementById('ratedCount');
  if (countSpan) {
    const rated = Object.values(userRatings).filter(v => v > 0).length;
    countSpan.textContent = `${rated} Active Skills Rated (Proficiency > 0)`;
  }
}

function setupSubmitHandler() {
  const submitBtn = document.getElementById('submitAssessmentBtn');
  if (!submitBtn) return;

  submitBtn.addEventListener('click', async () => {
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Calculating Skill Gap...';

    try {
      // Save target role
      try {
        await API.post('/api/roles/select', { roleId: currentRoleId });
      } catch (e) {}

      // Submit ratings
      await API.post('/api/assessment/submit', {
        ratings: userRatings,
        notes: userNotes
      });

      API.showToast('Ratings evaluated! Opening your dashboard...', 'success');
      setTimeout(() => {
        window.location.href = `${API.getBaseUrl()}/dashboard.html`;
      }, 700);

    } catch (err) {
      console.warn('Error saving to server, proceeding with local assessment:', err);
      window.location.href = `${API.getBaseUrl()}/dashboard.html`;
    }
  });
}
