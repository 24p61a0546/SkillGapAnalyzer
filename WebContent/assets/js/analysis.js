/**
 * Skill Gap Analysis Visualizer (Chart.js Radar & Bar Visualizations + Gap Matrix)
 */

let radarChart = null;
let barChart = null;
let currentReport = null;

document.addEventListener('DOMContentLoaded', async () => {
  await loadRolesDropdown();
  await runAnalysis();
});

async function loadRolesDropdown() {
  const select = document.getElementById('roleSelect');
  if (!select) return;

  try {
    const res = await API.get('/api/roles');
    if (res?.data && Array.isArray(res.data)) {
      select.innerHTML = res.data.map(r => `
        <option value="${r.id}">${r.title}</option>
      `).join('');

      select.addEventListener('change', () => {
        runAnalysis(select.value);
      });
    }
  } catch (err) {
    console.error('Could not load roles for dropdown', err);
  }
}

async function runAnalysis(roleId = null) {
  const container = document.getElementById('analysisContent');
  const endpoint = roleId ? `/api/analysis?roleId=${roleId}` : '/api/analysis';

  try {
    const res = await API.get(endpoint);
    if (!res || !res.data) throw new Error('Could not generate gap report');

    currentReport = res.data;

    // Sync dropdown selection with report
    const select = document.getElementById('roleSelect');
    if (select && currentReport.roleId) {
      select.value = currentReport.roleId;
    }

    renderSummaryMetrics(currentReport);
    renderCharts(currentReport);
    renderGapTable(currentReport);

  } catch (err) {
    console.error('Error generating analysis:', err);
    API.showToast(err.message || 'Error running analysis', 'error');
  }
}

function renderSummaryMetrics(report) {
  document.getElementById('reportRoleTitle').textContent = report.roleTitle;
  document.getElementById('reportReadiness').textContent = `${report.readinessPercentage}%`;
  document.getElementById('reportReadinessLevel').textContent = report.readinessLevel;
  document.getElementById('reportTotalSkills').textContent = report.totalSkillsRequired;
  document.getElementById('reportMetSkills').textContent = report.skillsMetCount;
  document.getElementById('reportGapSkills').textContent = report.skillsWithGapCount;
  document.getElementById('reportCriticalGaps').textContent = report.criticalGapCount;

  const badge = document.getElementById('readinessBadge');
  if (badge) {
    badge.className = 'badge';
    if (report.readinessPercentage >= 80) badge.classList.add('badge-success');
    else if (report.readinessPercentage >= 50) badge.classList.add('badge-warning');
    else badge.classList.add('badge-danger');
    badge.textContent = report.readinessLevel;
  }
}

function renderCharts(report) {
  const gaps = report.skillGaps || [];
  if (gaps.length === 0) return;

  const labels = gaps.map(g => g.skillName);
  const targetLevels = gaps.map(g => g.requiredProficiency);
  const userLevels = gaps.map(g => g.currentProficiency);

  // 1. Render Radar Chart (Skill Competency Web)
  const radarCtx = document.getElementById('radarChartCanvas');
  if (radarCtx) {
    if (radarChart) radarChart.destroy();

    radarChart = new Chart(radarCtx, {
      type: 'radar',
      data: {
        labels: labels,
        datasets: [
          {
            label: 'Benchmark Requirement',
            data: targetLevels,
            backgroundColor: 'rgba(79, 70, 229, 0.15)',
            borderColor: '#4f46e5',
            pointBackgroundColor: '#4f46e5',
            pointBorderColor: '#fff',
            pointHoverBackgroundColor: '#fff',
            pointHoverBorderColor: '#4f46e5',
            borderWidth: 2
          },
          {
            label: 'Your Current Level',
            data: userLevels,
            backgroundColor: 'rgba(16, 185, 129, 0.2)',
            borderColor: '#10b981',
            pointBackgroundColor: '#10b981',
            pointBorderColor: '#fff',
            pointHoverBackgroundColor: '#fff',
            pointHoverBorderColor: '#10b981',
            borderWidth: 2
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          r: {
            angleLines: { color: 'rgba(203, 213, 225, 0.6)' },
            grid: { color: 'rgba(203, 213, 225, 0.4)' },
            pointLabels: {
              font: { size: 11, family: 'Inter', weight: 600 },
              color: '#334155'
            },
            suggestedMin: 0,
            suggestedMax: 5,
            ticks: { stepSize: 1, backdropColor: 'transparent' }
          }
        },
        plugins: {
          legend: { position: 'top', labels: { font: { family: 'Inter', weight: 600 } } }
        }
      }
    });
  }

  // 2. Render Side-by-Side Bar Chart
  const barCtx = document.getElementById('barChartCanvas');
  if (barCtx) {
    if (barChart) barChart.destroy();

    barChart = new Chart(barCtx, {
      type: 'bar',
      data: {
        labels: labels,
        datasets: [
          {
            label: 'Required Level',
            data: targetLevels,
            backgroundColor: '#4f46e5',
            borderRadius: 6
          },
          {
            label: 'Your Level',
            data: userLevels,
            backgroundColor: '#10b981',
            borderRadius: 6
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          y: {
            beginAtZero: true,
            max: 5,
            ticks: { stepSize: 1 }
          },
          x: {
            ticks: {
              maxRotation: 45,
              minRotation: 25,
              font: { size: 10, family: 'Inter' }
            }
          }
        },
        plugins: {
          legend: { position: 'top', labels: { font: { family: 'Inter', weight: 600 } } }
        }
      }
    });
  }
}

function renderGapTable(report) {
  const tbody = document.getElementById('gapTableBody');
  if (!tbody) return;

  const gaps = report.skillGaps || [];
  if (gaps.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; padding: 2rem;">No skill benchmarks configured for this role.</td></tr>`;
    return;
  }

  tbody.innerHTML = gaps.map(g => {
    let statusBadge = '<span class="gap-badge gap-badge-zero"><i class="fas fa-check"></i> Target Met</span>';
    if (g.status === 'Minor Gap') {
      statusBadge = '<span class="gap-badge gap-badge-minor"><i class="fas fa-exclamation-triangle"></i> Minor Gap (-1)</span>';
    } else if (g.status === 'Critical Gap') {
      statusBadge = `<span class="gap-badge gap-badge-critical"><i class="fas fa-times-circle"></i> Critical Gap (-${g.gap})</span>`;
    }

    const dotsTarget = [1,2,3,4,5].map(i => `
      <span class="dot ${i <= g.requiredProficiency ? 'active-target' : ''}"></span>
    `).join('');

    const dotsUser = [1,2,3,4,5].map(i => `
      <span class="dot ${i <= g.currentProficiency ? 'active-user' : ''}"></span>
    `).join('');

    return `
      <tr>
        <td>
          <div class="skill-cell-name">${g.skillName}</div>
          <div class="skill-cell-cat">${g.category}</div>
        </td>
        <td>
          <span class="badge ${g.importance === 'Critical' ? 'badge-danger' : 'badge-primary'}">
            ${g.importance}
          </span>
        </td>
        <td>
          <div class="level-bar-wrap">
            <span style="font-weight: 700; width: 14px;">${g.requiredProficiency}</span>
            <div class="level-dots">${dotsTarget}</div>
          </div>
        </td>
        <td>
          <div class="level-bar-wrap">
            <span style="font-weight: 700; width: 14px;">${g.currentProficiency}</span>
            <div class="level-dots">${dotsUser}</div>
          </div>
        </td>
        <td>${statusBadge}</td>
        <td>
          ${g.gap > 0 ? `
            <a href="${API.getBaseUrl()}/recommendations.html?skillId=${g.skillId}" class="btn btn-outline btn-sm">
              <i class="fas fa-graduation-cap"></i> View Roadmap
            </a>
          ` : `
            <span style="color: var(--success); font-size: 0.85rem; font-weight: 600;">
              <i class="fas fa-check-double"></i> Ready
            </span>
          `}
        </td>
      </tr>
    `;
  }).join('');
}
