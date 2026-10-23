/**
 * Job Roles Explorer & Target Role Selection
 */

let allRoles = [];

document.addEventListener('DOMContentLoaded', async () => {
  await loadRoles();
});

async function loadRoles() {
  const container = document.getElementById('rolesContainer');
  if (!container) return;

  container.innerHTML = `
    <div style="text-align: center; padding: 3rem; color: var(--slate-500); grid-column: 1 / -1;">
      <i class="fas fa-spinner fa-spin" style="font-size: 2rem;"></i>
      <p style="margin-top: 1rem;">Loading Industry Job Roles...</p>
    </div>
  `;

  try {
    const res = await API.get('/api/roles');
    if (!res || !res.data) throw new Error('Could not load roles');

    allRoles = res.data;
    const user = await API.checkAuthStatus();
    const currentRoleId = user?.targetRoleId || 1;

    renderRoles(allRoles, currentRoleId);
  } catch (err) {
    container.innerHTML = `
      <div style="text-align: center; padding: 2rem; color: var(--danger); grid-column: 1 / -1;">
        <i class="fas fa-exclamation-triangle" style="font-size: 2rem;"></i>
        <p style="margin-top: 0.5rem;">Failed to load roles: ${err.message}</p>
      </div>
    `;
  }
}

function renderRoles(roles, currentTargetId) {
  const container = document.getElementById('rolesContainer');
  if (!container) return;

  container.innerHTML = roles.map(role => {
    const isCurrent = (role.id === currentTargetId);
    const skillsPreview = (role.requiredSkills || []).slice(0, 4);

    return `
      <div class="card" style="display: flex; flex-direction: column; justify-content: space-between; position: relative; border-color: ${isCurrent ? '#38bdf8' : 'var(--border-subtle)'}; box-shadow: ${isCurrent ? '0 0 25px rgba(56, 189, 248, 0.25)' : 'var(--shadow-sm)'};">
        ${isCurrent ? `
          <div style="position: absolute; top: -10px; right: 20px; background: var(--grad-primary); color: white; padding: 0.25rem 0.85rem; border-radius: var(--radius-full); font-size: 0.75rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em; box-shadow: 0 0 12px rgba(6, 182, 212, 0.5);">
            Active Target Role
          </div>
        ` : ''}

        <div>
          <div style="display: flex; align-items: center; gap: 0.85rem; margin-bottom: 1rem;">
            <div style="width: 48px; height: 48px; border-radius: var(--radius-md); background: rgba(99, 102, 241, 0.15); border: 1px solid rgba(99, 102, 241, 0.35); color: #38bdf8; display: flex; align-items: center; justify-content: center; font-size: 1.35rem;">
              <i class="${role.icon || 'fas fa-briefcase'}"></i>
            </div>
            <div>
              <h3 style="font-size: 1.25rem; font-weight: 800; color: var(--text-primary);">${role.title}</h3>
              <span class="badge badge-primary">${role.category}</span>
            </div>
          </div>

          <p style="color: var(--text-muted); font-size: 0.9rem; line-height: 1.6; margin-bottom: 1.25rem;">
            ${role.description}
          </p>

          <div style="background: rgba(15, 23, 42, 0.6); border: 1px solid var(--border-subtle); border-radius: var(--radius-md); padding: 0.75rem 1rem; margin-bottom: 1.25rem; font-size: 0.85rem; display: flex; justify-content: space-between; color: var(--text-secondary);">
            <div><strong style="color: var(--text-primary);">Avg. Salary:</strong> <span style="color: #38bdf8;">${role.avgSalary || '$90,000+'}</span></div>
            <div><strong style="color: var(--text-primary);">Demand:</strong> <span style="color: #34d399; font-weight: 700;">${role.demandLevel || 'High'}</span></div>
          </div>

          <div style="margin-bottom: 1.5rem;">
            <div style="font-size: 0.8rem; font-weight: 700; color: #38bdf8; text-transform: uppercase; margin-bottom: 0.5rem; letter-spacing: 0.05em;">Key Benchmark Skills:</div>
            <div style="display: flex; flex-wrap: wrap; gap: 0.4rem;">
              ${skillsPreview.map(s => `
                <span class="badge" style="background: var(--slate-100); color: var(--slate-700); font-size: 0.75rem;">
                  ${s.skillName} (Lvl ${s.requiredProficiency})
                </span>
              `).join('')}
              ${role.requiredSkills && role.requiredSkills.length > 4 ? `
                <span class="badge" style="background: var(--slate-100); color: var(--slate-500); font-size: 0.75rem;">
                  +${role.requiredSkills.length - 4} more
                </span>
              ` : ''}
            </div>
          </div>
        </div>

        <div style="display: flex; gap: 0.75rem; margin-top: auto;">
          ${isCurrent ? `
            <a href="${API.getBaseUrl()}/analysis.html" class="btn btn-primary" style="flex: 1;">
              <i class="fas fa-chart-pie"></i> View Gap Report
            </a>
          ` : `
            <button onclick="selectTargetRole(${role.id}, '${role.title}')" class="btn btn-outline" style="flex: 1;">
              <i class="fas fa-bullseye"></i> Set as Target Role
            </button>
            <a href="${API.getBaseUrl()}/analysis.html?roleId=${role.id}" class="btn btn-secondary" title="Simulate Gap Analysis for this role">
              <i class="fas fa-eye"></i>
            </a>
          `}
        </div>
      </div>
    `;
  }).join('');
}

window.selectTargetRole = async function(roleId, roleTitle) {
  try {
    const saved = sessionStorage.getItem('skillgap_user');
    if (saved) {
      try {
        const u = JSON.parse(saved);
        u.targetRoleId = roleId;
        u.targetRoleTitle = roleTitle;
        sessionStorage.setItem('skillgap_user', JSON.stringify(u));
      } catch (e) {}
    }
    await API.post('/api/roles/select', { roleId });
    API.showToast(`Target role successfully changed to ${roleTitle}!`, 'success');
    setTimeout(() => {
      window.location.reload();
    }, 600);
  } catch (err) {
    API.showToast(err.message || 'Failed to update target role.', 'error');
  }
};
