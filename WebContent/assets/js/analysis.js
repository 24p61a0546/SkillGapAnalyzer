/**
 * Skill Gap Analysis Visualizer (Chart.js Radar & Bar Visualizations + Gap Matrix)
 * Styled for Futuristic Dark Navy Theme
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

  // 1. Render Radar Chart (Skill Competency Web) with Futuristic Glowing Neon
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
            backgroundColor: 'rgba(56, 189, 248, 0.2)',
            borderColor: '#38bdf8',
            pointBackgroundColor: '#38bdf8',
            pointBorderColor: '#0f172a',
            pointHoverBackgroundColor: '#fff',
            pointHoverBorderColor: '#38bdf8',
            borderWidth: 2
          },
          {
            label: 'Your Current Level',
            data: userLevels,
            backgroundColor: 'rgba(168, 85, 247, 0.25)',
            borderColor: '#c084fc',
            pointBackgroundColor: '#c084fc',
            pointBorderColor: '#0f172a',
            pointHoverBackgroundColor: '#fff',
            pointHoverBorderColor: '#c084fc',
            borderWidth: 2
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          r: {
            angleLines: { color: 'rgba(255, 255, 255, 0.12)' },
            grid: { color: 'rgba(255, 255, 255, 0.08)' },
            pointLabels: {
              font: { size: 11, family: 'Inter', weight: 600 },
              color: '#cbd5e1'
            },
            suggestedMin: 0,
            suggestedMax: 5,
            ticks: {
              stepSize: 1,
              backdropColor: 'transparent',
              color: '#94a3b8'
            }
          }
        },
        plugins: {
          legend: {
            position: 'top',
            labels: {
              font: { family: 'Inter', weight: 600 },
              color: '#f8fafc'
            }
          }
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
            label: 'Benchmark Requirement',
            data: targetLevels,
            backgroundColor: 'rgba(56, 189, 248, 0.85)',
            borderRadius: 6
          },
          {
            label: 'Your Current Level',
            data: userLevels,
            backgroundColor: 'rgba(168, 85, 247, 0.85)',
            borderRadius: 6
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          y: {
            suggestedMin: 0,
            suggestedMax: 5,
            ticks: { stepSize: 1, color: '#94a3b8' },
            grid: { color: 'rgba(255, 255, 255, 0.08)' }
          },
          x: {
            grid: { display: false },
            ticks: {
              maxRotation: 45,
              minRotation: 25,
              font: { size: 10, family: 'Inter' },
              color: '#cbd5e1'
            }
          }
        },
        plugins: {
          legend: {
            position: 'top',
            labels: {
              font: { family: 'Inter', weight: 600 },
              color: '#f8fafc'
            }
          }
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
    tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; padding: 2rem; color: var(--text-muted);">No skill benchmarks configured for this role.</td></tr>`;
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
            <span style="font-weight: 700; width: 14px; color: #38bdf8;">${g.requiredProficiency}</span>
            <div class="level-dots">${dotsTarget}</div>
          </div>
        </td>
        <td>
          <div class="level-bar-wrap">
            <span style="font-weight: 700; width: 14px; color: #c084fc;">${g.currentProficiency}</span>
            <div class="level-dots">${dotsUser}</div>
          </div>
        </td>
        <td>${statusBadge}</td>
        <td>
          ${g.gap > 0 ? `
            <a href="recommendations.html?skillId=${g.skillId}" class="btn btn-outline btn-sm">
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
