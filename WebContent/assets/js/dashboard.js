/**
 * Dashboard Controller:
 * Displays User Name, Current Skills, Missing Skills, Skill Gap Percentage,
 * Recommended Skills, and Learning Roadmap with Count-Up Animations.
 */

document.addEventListener('DOMContentLoaded', async () => {
  initLocalProfile();
  await loadDashboard();
});

function initLocalProfile() {
  const saved = sessionStorage.getItem('skillgap_user');
  if (saved) {
    try {
      const u = JSON.parse(saved);
      if (u.fullName) {
        const el = document.getElementById('userName');
        if (el) el.textContent = u.fullName;
        const nav = document.getElementById('navUserName');
        if (nav) nav.innerHTML = `<i class="fas fa-user-astronaut"></i> ${u.fullName}`;
      }
      if (u.targetRoleTitle) {
        const el = document.getElementById('userTargetRole');
        if (el) el.textContent = u.targetRoleTitle;
      }
      if (u.email) {
        const el = document.getElementById('userEmail');
        if (el) el.textContent = u.email;
      }
    } catch (e) {}
  }
}

function animateCountUp(elementId, targetValue, suffix = '') {
  const el = document.getElementById(elementId);
  if (!el) return;
  const target = parseFloat(targetValue) || 0;
  if (target === 0) {
    el.textContent = `0${suffix}`;
    return;
  }
  let current = 0;
  const duration = 1000;
  const stepTime = 20;
  const steps = duration / stepTime;
  const increment = target / steps;

  const timer = setInterval(() => {
    current += increment;
    if (current >= target) {
      current = target;
      clearInterval(timer);
    }
    el.textContent = `${Number.isInteger(target) ? Math.round(current) : current.toFixed(1)}${suffix}`;
  }, stepTime);
}

async function loadDashboard() {
  try {
    const res = await API.get('/api/dashboard');
    if (!res || !res.data) return;

    const data = res.data;
    const user = data.user || {};
    const role = data.targetRole || {};
    const report = data.report || {};

    // 1. Populate User Header
    if (user.fullName) {
      document.getElementById('userName').textContent = user.fullName;
      const nav = document.getElementById('navUserName');
      if (nav) nav.innerHTML = `<i class="fas fa-user-astronaut"></i> ${user.fullName}`;
    }
    if (user.email) {
      document.getElementById('userEmail').textContent = user.email;
    }
    if (role.title) {
      document.getElementById('userTargetRole').textContent = role.title;
      document.getElementById('targetRoleTitle').textContent = role.title;
      document.getElementById('targetRoleDesc').textContent = role.description || '';
      document.getElementById('targetRoleSalary').textContent = role.avgSalary || '$90,000+';
      document.getElementById('targetRoleDemand').textContent = role.demandLevel || 'High';
    }

    // 2. Metrics & Percentages with Count-Up Animations
    const readinessPct = data.readinessPercentage !== undefined ? data.readinessPercentage : 0;
    const gapPct = data.skillGapPercentage !== undefined
      ? data.skillGapPercentage
      : Math.round((100 - readinessPct) * 10) / 10;

    animateCountUp('kpiReadiness', readinessPct, '%');
    animateCountUp('kpiSkillGap', gapPct, '%');
    document.getElementById('userReadinessBadge').textContent = `${data.readinessLevel || 'Evaluated'} (${readinessPct}% Readiness)`;

    const currentSkills = data.currentSkills || [];
    const missingSkills = data.missingSkills || [];

    animateCountUp('kpiCurrentCount', currentSkills.length);
    animateCountUp('kpiMissingCount', missingSkills.length);

    // Linear Progress Meters
    setTimeout(() => {
      const readFill = document.getElementById('readinessFill');
      if (readFill) readFill.style.width = `${readinessPct}%`;
      const readLabel = document.getElementById('readinessScoreLabel');
      if (readLabel) readLabel.textContent = `${readinessPct}% Met`;

      const gapFill = document.getElementById('gapFill');
      if (gapFill) gapFill.style.width = `${gapPct}%`;
      const gapLabel = document.getElementById('gapScoreLabel');
      if (gapLabel) gapLabel.textContent = `${gapPct}% Gap`;
    }, 150);

    // 3. Render Missing Skills
    renderMissingSkills(missingSkills);

    // 4. Render Current Skills
    renderCurrentSkills(currentSkills);

    // 5. Render Recommended Skills
    renderRecommendedSkills(data.topGaps || missingSkills.slice(0, 4));

    // 6. Render Learning Roadmap
    renderLearningRoadmap(data.learningRoadmap || []);

  } catch (err) {
    console.error('Failed to load dashboard data:', err);
  }
}

function renderMissingSkills(missing) {
  const container = document.getElementById('missingSkillsContainer');
  const badge = document.getElementById('missingBadge');
  if (!container) return;

  if (badge) {
    badge.textContent = `${missing.length} Skill${missing.length === 1 ? '' : 's'} with Gaps`;
  }

  if (missing.length === 0) {
    container.innerHTML = `
      <div style="text-align: center; padding: 2.25rem; background: rgba(16, 185, 129, 0.12); border-radius: var(--radius-lg); border: 1px solid rgba(16, 185, 129, 0.35);">
        <i class="fas fa-check-circle" style="font-size: 2.5rem; color: #34d399; margin-bottom: 0.5rem;"></i>
        <h4 style="color: #6ee7b7; font-size: 1.2rem; font-weight: 700;">No Missing Skills Detected</h4>
        <p style="color: #a7f3d0; font-size: 0.925rem; margin-top: 0.35rem;">
          Your technical profile matches or exceeds every benchmark for this target role!
        </p>
      </div>
    `;
    return;
  }

  container.innerHTML = missing.map(m => {
    const isZero = m.currentProficiency === 0;
    const isCritical = m.importance === 'Critical' || m.gap >= 2;

    return `
      <div class="missing-skill-item">
        <div style="flex: 1; min-width: 200px;">
          <div style="display: flex; align-items: center; gap: 0.5rem; flex-wrap: wrap;">
            <strong style="font-size: 1.05rem; color: var(--text-primary);">${m.skillName}</strong>
            <span class="badge ${isCritical ? 'badge-danger' : 'badge-warning'}">
              ${isCritical ? 'Critical Gap' : 'Moderate Gap'}
            </span>
            <span class="badge badge-info">${m.category || 'Competency'}</span>
          </div>
          <div style="font-size: 0.85rem; color: var(--text-muted); margin-top: 0.35rem;">
            Current Level: <strong style="color: ${isZero ? '#f87171' : '#38bdf8'}">${m.currentProficiency}/5</strong> &bull;
            Required Level: <strong style="color: #38bdf8">${m.requiredProficiency}/5</strong> &bull;
            Deficit: <strong style="color: #f87171">-${m.gap} Level${m.gap > 1 ? 's' : ''}</strong>
          </div>
        </div>

        <div style="display: flex; gap: 0.5rem; align-items: center;">
          <a href="recommendations.html?skillId=${m.skillId}" class="btn btn-outline btn-sm">
            <i class="fas fa-book-reader"></i> View Courses &rarr;
          </a>
        </div>
      </div>
    `;
  }).join('');
}

function renderCurrentSkills(skills) {
  const container = document.getElementById('currentSkillsContainer');
  const badge = document.getElementById('currentBadge');
  if (!container) return;

  if (badge) {
    badge.textContent = `${skills.length} Skill${skills.length === 1 ? '' : 's'} Rated`;
  }

  if (skills.length === 0) {
    container.innerHTML = `
      <div style="text-align: center; padding: 2rem; grid-column: 1 / -1; color: var(--text-muted);">
        <p>No skills have been evaluated yet.</p>
        <a href="assessment.html" class="btn btn-primary btn-sm" style="margin-top: 0.75rem;">
          <i class="fas fa-sliders-h"></i> Take Skill Assessment
        </a>
      </div>
    `;
    return;
  }

  const levelLabels = ['', 'Novice', 'Beginner', 'Competent', 'Proficient', 'Expert'];

  container.innerHTML = skills.map(s => {
    const pct = (s.proficiency / 5) * 100;
    const label = levelLabels[s.proficiency] || `Level ${s.proficiency}`;

    return `
      <div class="skill-stat-card">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <strong style="color: var(--text-primary); font-size: 0.95rem;">${s.skillName}</strong>
          <span class="badge badge-primary">${label} (${s.proficiency}/5)</span>
        </div>
        <div style="font-size: 0.8rem; color: var(--text-muted);">${s.category || 'Capability'}</div>
        <div class="progress-bar-container" style="height: 6px; margin-top: 0.35rem;">
          <div class="progress-bar-fill" style="width: ${pct}%;"></div>
        </div>
      </div>
    `;
  }).join('');
}

function renderRecommendedSkills(topGaps) {
  const container = document.getElementById('recommendedSkillsContainer');
  if (!container) return;

  if (!topGaps || topGaps.length === 0) {
    container.innerHTML = `
      <div style="grid-column: 1 / -1; text-align: center; padding: 1.5rem; color: #34d399;">
        <i class="fas fa-star" style="font-size: 1.5rem; margin-bottom: 0.25rem;"></i>
        <p>You have met your core skill milestones! Explore advanced topics in the roadmap.</p>
      </div>
    `;
    return;
  }

  container.innerHTML = topGaps.map(g => `
    <div class="skill-stat-card" style="border-top: 3px solid #f59e0b;">
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <strong style="color: var(--text-primary); font-size: 0.95rem;">${g.skillName}</strong>
        <span class="badge badge-warning">Priority Focus</span>
      </div>
      <div style="font-size: 0.85rem; color: var(--text-muted); margin: 0.25rem 0;">
        Requires <strong style="color: #38bdf8">Level ${g.requiredProficiency}</strong> (Current: ${g.currentProficiency || 0})
      </div>
      <div style="display: flex; justify-content: space-between; align-items: center; margin-top: auto; padding-top: 0.5rem;">
        <span style="font-size: 0.785rem; color: #f87171; font-weight: 700;">Deficit: -${g.gap} Levels</span>
        <a href="recommendations.html?skillId=${g.skillId}" class="btn btn-outline btn-sm" style="padding: 0.25rem 0.65rem; font-size: 0.785rem;">
          Explore Courses &rarr;
        </a>
      </div>
    </div>
  `).join('');
}

function renderLearningRoadmap(roadmap) {
  const container = document.getElementById('learningRoadmapContainer');
  if (!container) return;

  if (!roadmap || roadmap.length === 0) {
    container.innerHTML = `
      <div style="grid-column: 1 / -1; text-align: center; padding: 2rem; color: var(--text-muted);">
        <p>Your current skill levels meet requirements for this role. Check back when targeting a new career path!</p>
      </div>
    `;
    return;
  }

  container.innerHTML = roadmap.slice(0, 6).map(r => {
    let icon = 'fa-book-open';
    if (r.type === 'Hands-on Project') icon = 'fa-hammer';
    if (r.type === 'Video Tutorial') icon = 'fa-play-circle';
    if (r.type === 'Practice') icon = 'fa-code';

    let diffBadge = 'badge-primary';
    if (r.difficultyLevel === 'Beginner') diffBadge = 'badge-success';
    if (r.difficultyLevel === 'Advanced') diffBadge = 'badge-danger';
    if (r.difficultyLevel === 'Intermediate') diffBadge = 'badge-warning';

    return `
      <div class="roadmap-card">
        <div>
          <div class="roadmap-badge-bar">
            <span class="badge ${diffBadge}">${r.difficultyLevel || 'Practical'}</span>
            <span class="badge badge-primary"><i class="fas ${icon}"></i> ${r.type}</span>
          </div>

          <h3 class="roadmap-title" style="margin-top: 0.85rem;">${r.title}</h3>
          <div style="font-size: 0.825rem; color: #38bdf8; font-weight: 600; margin-top: 0.35rem;">
            Skill: ${r.skillName || 'Competency'}
          </div>
        </div>

        <div>
          <div class="roadmap-meta" style="margin-bottom: 0.85rem;">
            <span><i class="fas fa-university"></i> ${r.provider}</span>
            <span><i class="fas fa-clock"></i> ${r.estimatedHours || 'Self-paced'}</span>
          </div>
          <a href="${r.url}" target="_blank" rel="noopener noreferrer" class="btn btn-primary btn-sm" style="width: 100%;">
            <i class="fas fa-external-link-alt"></i> Start Learning
          </a>
        </div>
      </div>
    `;
  }).join('');
}
