/**
 * Skill Gap Analyzer - Core API Client & UI Helpers
 */

const API = {
  // Base path automatically detects context path (e.g. /SkillGapAnalyzer or /)
  getBaseUrl() {
    const path = window.location.pathname;
    const segments = path.split('/').filter(Boolean);
    if (segments.length > 0 && !segments[0].endsWith('.html') && segments[0] !== 'api') {
      return `/${segments[0]}`;
    }
    return '';
  },

  async request(endpoint, options = {}) {
    const url = `${this.getBaseUrl()}${endpoint}`;
    const defaultHeaders = {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    };

    const config = {
      ...options,
      headers: {
        ...defaultHeaders,
        ...(options.headers || {})
      }
    };

    try {
      const response = await fetch(url, config);
      const data = await response.json().catch(() => null);

      if (response.status === 401) {
        // Unauthorized - session expired
        if (!window.location.pathname.endsWith('login.html') && !window.location.pathname.endsWith('register.html') && !window.location.pathname.endsWith('index.html')) {
          this.showToast('Session expired. Please log in.', 'error');
          setTimeout(() => {
            window.location.href = `${this.getBaseUrl()}/login.html`;
          }, 1200);
        }
        throw new Error(data?.message || 'Unauthorized');
      }

      if (!response.ok) {
        throw new Error(data?.message || `Request failed with status ${response.status}`);
      }

      return data;
    } catch (err) {
      console.error(`[API Error] ${endpoint}:`, err);
      throw err;
    }
  },

  get(endpoint) {
    return this.request(endpoint, { method: 'GET' });
  },

  post(endpoint, body) {
    return this.request(endpoint, {
      method: 'POST',
      body: JSON.stringify(body)
    });
  },

  showToast(message, type = 'info') {
    let container = document.querySelector('.toast-container');
    if (!container) {
      container = document.createElement('div');
      container.className = 'toast-container';
      document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    let iconClass = 'fa-info-circle';
    if (type === 'success') iconClass = 'fa-check-circle';
    if (type === 'error') iconClass = 'fa-exclamation-triangle';
    if (type === 'warning') iconClass = 'fa-exclamation-circle';

    toast.innerHTML = `
      <i class="fas ${iconClass}" style="font-size: 1.25rem;"></i>
      <span style="font-size: 0.9rem; font-weight: 500;">${message}</span>
    `;

    container.appendChild(toast);
    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateX(100%)';
      toast.style.transition = 'all 0.3s ease';
      setTimeout(() => toast.remove(), 300);
    }, 4000);
  },

  async checkAuthStatus() {
    try {
      const res = await this.get('/api/auth/me');
      return res?.data || null;
    } catch {
      return null;
    }
  },

  async logout() {
    try {
      await this.get('/api/auth/logout');
      this.showToast('Logged out successfully', 'success');
      setTimeout(() => {
        window.location.href = `${this.getBaseUrl()}/login.html`;
      }, 800);
    } catch (err) {
      window.location.href = `${this.getBaseUrl()}/login.html`;
    }
  }
};
