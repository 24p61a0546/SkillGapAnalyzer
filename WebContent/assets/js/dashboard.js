/**
 * Dashboard Controller: Loads user profile, target role metrics, readiness score and top gaps
 */

document.addEventListener('DOMContentLoaded', async () => {
  await loadDashboard();
});

async function loadDashboard() {
  try {
    const res = await API.get('/api/dashboard');
    if (!res || !res.data) return;

    const data = res.data;
    const user = data.user;
    const role = data.targetRole;
    const report = data.report;

    // 1. Populate User Banner
    document.getElementById('userName').textContent = user.fullName || 'Student';
    document.getElementById('userDept').textContent = user.department || 'Computer Science';
    document.getElementById('userYear').textContent = user.yearOfStudy || '3rd Year';
    document.getElementById('navUserEmail').textContent = user.email || '';

    // 2. Populate Target Role
    if (role) {
      document.getElementById('targetRoleTitle').textContent = role.title;
      document.getElementById('targetRoleDesc').textContent = role.description;
      document.getElementById('targetRoleSalary').textContent = role.avgSalary || '$90,000+';
      document.getElementById('targetRoleDemand').textContent = role.demandLevel || 'High Demand';
      document.getElementById('targetRoleCategory').textContent = role.category || 'Tech';
    }

    // 3. Populate KPIs
    const readinessPct = data.readinessPercentage || 0;
    document.getElementById('kpiReadiness').textContent = `${readinessPct}%`;
    document.getElementById('kpiReadinessLevel').textContent = data.readinessLevel || 'Foundational Phase';
    document.getElementById('readinessFill').style.width = `${readinessPct}%`;

    document.getElementById('kpiAssessed').textContent = data.assessedCount || 0;
    document.getElementById('kpiSkillsMet').textContent = data.skillsMetCount || 0;
    document.getElementById('kpiCriticalGaps').textContent = data.criticalGapCount || 0;

    // 4. Populate Top Gaps List
    const gapsList = document.getElementById('topGapsList');
    if (gapsList) {
      if (data.topGaps && data.topGaps.length > 0) {
        gapsList.innerHTML = data.topGaps.map(g => `
          <div class="gap-item">
            <div class="gap-item-info">
              <h5>${g.skillName}</h5>
              <span>Current: Level ${g.currentProficiency} / Target: Level ${g.requiredProficiency}</span>
            </div>
            <span class="badge ${g.gap >= 2 ? 'badge-danger' : 'badge-warning'}">
              Gap: -${g.gap} Level${g.gap > 1 ? 's' : ''}
            </span>
          </div>
        `).join('');
      } else {
        gapsList.innerHTML = `
          <div style="text-align: center; padding: 1.5rem; color: var(--success);">
            <i class="fas fa-check-circle" style="font-size: 2rem; margin-bottom: 0.5rem;"></i>
            <p><strong>Outstanding!</strong> You currently meet all benchmark requirements for this role.</p>
          </div>
        `;
      }
    }

  } catch (err) {
    console.error('Failed to load dashboard:', err);
  }
}
