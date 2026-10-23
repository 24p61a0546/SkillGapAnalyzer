/**
 * Authentication handling: Login and Registration are disabled.
 * The application operates with direct open student access.
 */
document.addEventListener('DOMContentLoaded', () => {
  // If invoked anywhere, redirect directly to index.html
  if (window.location.pathname.endsWith('login.html') || window.location.pathname.endsWith('register.html')) {
    window.location.replace('index.html');
  }
});
