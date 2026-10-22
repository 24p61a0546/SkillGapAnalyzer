/**
 * Personalized Learning Recommendations & Roadmaps
 */

let allRecommendations = [];

document.addEventListener('DOMContentLoaded', async () => {
  const urlParams = new URLSearchParams(window.location.search);
  const skillId = urlParams.get('skillId');
  await loadRecommendations(skillId);
});

async function loadRecommendations(skillId = null) {
  const container = document.getElementById('recommendationsContainer');
  if (!container) return;

  const endpoint = skillId ? `/api/recommendations?skillId=${skillId}` : '/api/recommendations';

  container.innerHTML = `
    <div style="text-align: center; padding: 3rem; color: var(--slate-500); grid-column: 1 / -1;">
      <i class="fas fa-spinner fa-spin" style="font-size: 2rem;"></i>
      <p style="margin-top: 1rem;">Curating tailored learning resources...</p>
    </div>
  `;

  try {
    const res = await API.get(endpoint);
    if (!res || !res.data) throw new Error('Could not load recommendations');

    allRecommendations = res.data;
    renderRecommendations(allRecommendations);
  } catch (err) {
    container.innerHTML = `
      <div style="text-align: center; padding: 2rem; color: var(--danger); grid-column: 1 / -1;">
        <i class="fas fa-exclamation-circle" style="font-size: 2rem;"></i>
        <p style="margin-top: 0.5rem;">Failed to load recommendations: ${err.message}</p>
      </div>
    `;
  }
}

function renderRecommendations(items) {
  const container = document.getElementById('recommendationsContainer');
  if (!container) return;

  if (items.length === 0) {
    container.innerHTML = `
      <div style="text-align: center; padding: 3rem; background: white; border-radius: 12px; grid-column: 1 / -1;">
        <i class="fas fa-award" style="font-size: 3rem; color: var(--success); margin-bottom: 1rem;"></i>
        <h3>No Urgent Gaps Found!</h3>
        <p style="color: var(--slate-500); max-width: 500px; margin: 0.5rem auto 1.5rem;">
          You currently meet or exceed the benchmarks for your target role. You can explore advanced certifications or practice coding challenges.
        </p>
        <a href="${API.getBaseUrl()}/roles.html" class="btn btn-primary">
          <i class="fas fa-compass"></i> Explore Other High-Demand Roles
        </a>
      </div>
    `;
    return;
  }

  container.innerHTML = items.map(r => {
    let typeIcon = 'fa-book-open';
    if (r.type === 'Course') typeIcon = 'fa-graduation-cap';
    if (r.type === 'Hands-on Project') typeIcon = 'fa-hammer';
    if (r.type === 'Video Tutorial') typeIcon = 'fa-play-circle';
    if (r.type === 'Practice') typeIcon = 'fa-code';

    let diffBadge = 'badge-primary';
    if (r.difficultyLevel === 'Beginner') diffBadge = 'badge-success';
    if (r.difficultyLevel === 'Advanced') diffBadge = 'badge-danger';
    if (r.difficultyLevel === 'Intermediate') diffBadge = 'badge-warning';

    return `
      <div class="recom-card">
        <div>
          <div class="recom-header">
            <span class="badge ${diffBadge}">
              <i class="fas fa-layer-group"></i> ${r.difficultyLevel}
            </span>
            <span class="badge badge-primary">
              <i class="fas ${typeIcon}"></i> ${r.type}
            </span>
          </div>

          <h3 class="recom-title" style="margin-top: 1rem;">${r.title}</h3>

          <div style="margin: 0.75rem 0;">
            <span style="font-size: 0.85rem; font-weight: 700; color: var(--primary);">
              <i class="fas fa-check"></i> Skill Focus: ${r.skillName || 'Technical Capability'}
            </span>
          </div>

          <div class="recom-meta" style="margin-top: 1rem;">
            <span><i class="fas fa-university"></i> ${r.provider}</span>
            <span><i class="fas fa-clock"></i> ${r.estimatedHours || 'Self-paced'}</span>
          </div>
        </div>

        <div style="margin-top: 1.5rem;">
          <a href="${r.url}" target="_blank" rel="noopener noreferrer" class="btn btn-primary" style="width: 100%;">
            Start Learning <i class="fas fa-external-link-alt" style="font-size: 0.8rem;"></i>
          </a>
        </div>
      </div>
    `;
  }).join('');
}
