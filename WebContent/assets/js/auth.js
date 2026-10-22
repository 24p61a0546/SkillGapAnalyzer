/**
 * Authentication handling: Login and Registration
 */

document.addEventListener('DOMContentLoaded', () => {
  const loginForm = document.getElementById('loginForm');
  const registerForm = document.getElementById('registerForm');

  // Handle Login
  if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const email = document.getElementById('email').value.trim();
      const password = document.getElementById('password').value;
      const submitBtn = loginForm.querySelector('button[type="submit"]');

      if (!email || !password) {
        API.showToast('Please enter both email and password.', 'warning');
        return;
      }

      submitBtn.disabled = true;
      submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Authenticating...';

      try {
        const response = await API.post('/api/auth/login', { email, password });
        API.showToast('Login successful! Redirecting...', 'success');
        setTimeout(() => {
          window.location.href = `${API.getBaseUrl()}/dashboard.html`;
        }, 1000);
      } catch (err) {
        API.showToast(err.message || 'Login failed. Please check credentials.', 'error');
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="fas fa-sign-in-alt"></i> Sign In to Account';
      }
    });
  }

  // Handle Registration
  if (registerForm) {
    // Load job roles dynamically into dropdown if available
    loadRolesDropdown();

    registerForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const fullName = document.getElementById('fullName').value.trim();
      const email = document.getElementById('email').value.trim();
      const password = document.getElementById('password').value;
      const confirmPassword = document.getElementById('confirmPassword').value;
      const department = document.getElementById('department').value;
      const yearOfStudy = document.getElementById('yearOfStudy').value;
      const targetRoleId = parseInt(document.getElementById('targetRoleId').value, 10);
      const submitBtn = registerForm.querySelector('button[type="submit"]');

      if (password !== confirmPassword) {
        API.showToast('Passwords do not match.', 'warning');
        return;
      }

      if (password.length < 6) {
        API.showToast('Password must be at least 6 characters.', 'warning');
        return;
      }

      submitBtn.disabled = true;
      submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Creating Profile...';

      try {
        await API.post('/api/auth/register', {
          fullName,
          email,
          password,
          department,
          yearOfStudy,
          targetRoleId
        });

        API.showToast('Account created successfully! Redirecting...', 'success');
        setTimeout(() => {
          window.location.href = `${API.getBaseUrl()}/dashboard.html`;
        }, 1200);
      } catch (err) {
        API.showToast(err.message || 'Registration failed. Try again.', 'error');
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="fas fa-user-plus"></i> Create Student Account';
      }
    });
  }
});

async function loadRolesDropdown() {
  const select = document.getElementById('targetRoleId');
  if (!select) return;

  try {
    const res = await API.get('/api/roles');
    if (res?.data && Array.isArray(res.data)) {
      select.innerHTML = res.data.map(r => `<option value="${r.id}">${r.title} (${r.category})</option>`).join('');
    }
  } catch (err) {
    console.warn('Could not load roles list dynamically, using static fallback');
  }
}
