/**
 * Skill Assessment: Interactive Proficiency Rating Matrix
 */

let allSkills = [];
let currentCategory = 'All';
const userRatings = {};
const userNotes = {};

document.addEventListener('DOMContentLoaded', async () => {
  await loadAssessmentSkills();
  setupFilterTabs();
  setupSubmitHandler();
});

async function loadAssessmentSkills() {
  const container = document.getElementById('skillsContainer');
  if (!container) return;

  container.innerHTML = `
    <div style="text-align: center; padding: 3rem; color: var(--slate-500);">
      <i class="fas fa-spinner fa-spin" style="font-size: 2rem;"></i>
      <p style="margin-top: 1rem;">Loading skills catalog...</p>
    </div>
  `;

  try {
    const res = await API.get('/api/assessment/skills');
    if (!res || !res.data) throw new Error('Could not retrieve skills catalog');

    allSkills = res.data;

    // Initialize ratings from server
    allSkills.forEach(s => {
      if (s.currentRating > 0) {
        userRatings[s.id] = s.currentRating;
      }
    });

    renderCategories(allSkills);
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

function renderCategories(skills) {
  const tabsContainer = document.getElementById('categoryTabs');
  if (!tabsContainer) return;

  const categories = ['All', ...new Set(skills.map(s => s.category))];
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

  const filtered = (currentCategory === 'All')
    ? allSkills
    : allSkills.filter(s => s.category === currentCategory);

  if (filtered.length === 0) {
    container.innerHTML = `<p style="text-align: center; padding: 2rem; color: var(--slate-400);">No skills found in this category.</p>`;
    return;
  }

  container.innerHTML = filtered.map(skill => {
    const selectedLevel = userRatings[skill.id] || 0;

    return `
      <div class="skill-card" id="skill-card-${skill.id}">
        <div class="skill-meta">
          <h4>
            <i class="fas fa-check-circle" style="color: ${selectedLevel > 0 ? 'var(--success)' : 'var(--slate-300)'};"></i>
            ${skill.name}
          </h4>
          <span class="badge badge-primary" style="margin-top: 0.35rem;">${skill.category}</span>
          <p>${skill.description || 'Core technical capability.'}</p>
        </div>

        <div class="rating-control-wrap">
          <div class="rating-pills">
            ${[1, 2, 3, 4, 5].map(lvl => `
              <div class="rating-pill ${selectedLevel === lvl ? 'selected' : ''}"
                   onclick="selectRating(${skill.id}, ${lvl})">
                ${lvl}
              </div>
            `).join('')}
          </div>
          <div class="level-legend">
            <span>1: Novice</span>
            <span>2: Beginner</span>
            <span>3: Competent</span>
            <span>4: Proficient</span>
            <span>5: Expert</span>
          </div>
          <input type="text" 
                 class="skill-notes-input" 
                 placeholder="Optional: Note practical experience or projects..."
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
    const count = Object.keys(userRatings).length;
    countSpan.textContent = `${count} of ${allSkills.length} Skills Rated`;
  }
}

function setupFilterTabs() {
  // handled dynamically in renderCategories
}

function setupSubmitHandler() {
  const submitBtn = document.getElementById('submitAssessmentBtn');
  if (!submitBtn) return;

  submitBtn.addEventListener('click', async () => {
    const ratingKeys = Object.keys(userRatings);
    if (ratingKeys.length === 0) {
      API.showToast('Please rate at least one skill before submitting.', 'warning');
      return;
    }

    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Analyzing Gaps...';

    try {
      await API.post('/api/assessment/submit', {
        ratings: userRatings,
        notes: userNotes
      });

      API.showToast('Assessment saved! Generating your Skill Gap Report...', 'success');
      setTimeout(() => {
        window.location.href = `${API.getBaseUrl()}/analysis.html`;
      }, 1200);
    } catch (err) {
      API.showToast(err.message || 'Failed to submit assessment.', 'error');
      submitBtn.disabled = false;
      submitBtn.innerHTML = '<i class="fas fa-chart-line"></i> Save & View Gap Analysis';
    }
  });
}
