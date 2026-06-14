/**
 * Toast Notification System - Custom floating notifications
 * Replaces alert() with beautiful, non-intrusive toast messages
 */

class Toast {
  constructor(message, type = 'info', duration = 3000) {
    this.message = message;
    this.type = type; // 'success', 'error', 'info', 'warning'
    this.duration = duration;
    this.element = null;
    this.show();
  }

  show() {
    // Create container if doesn't exist
    if (!document.getElementById('toast-container')) {
      const container = document.createElement('div');
      container.id = 'toast-container';
      container.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 9999;
        display: flex;
        flex-direction: column;
        gap: 10px;
        pointer-events: none;
      `;
      document.body.appendChild(container);
    }

    const container = document.getElementById('toast-container');

    // Create toast element
    this.element = document.createElement('div');
    this.element.className = `toast toast--${this.type}`;
    this.element.style.cssText = `
      padding: 16px 20px;
      border-radius: 8px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      display: flex;
      align-items: center;
      gap: 12px;
      min-width: 300px;
      animation: slideIn 0.3s ease-out;
      pointer-events: auto;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
      font-size: 14px;
      font-weight: 500;
      backdrop-filter: blur(10px);
    `;

    // Set styling based on type
    const styling = this.getTypeStyling();
    this.element.style.backgroundColor = styling.bg;
    this.element.style.color = styling.text;
    this.element.style.borderLeft = styling.border;

    // Create content with icon and message
    const icon = this.getIcon();
    const messageSpan = document.createElement('span');
    messageSpan.textContent = this.message;

    //this.element.appendChild(icon);
    this.element.appendChild(messageSpan);

    // Add close button
    const closeBtn = document.createElement('button');
    closeBtn.innerHTML = '✕';
    closeBtn.style.cssText = `
      background: none;
      border: none;
      color: inherit;
      cursor: pointer;
      font-size: 18px;
      padding: 0;
      margin-left: auto;
      opacity: 0.6;
      transition: opacity 0.2s;
    `;
    closeBtn.onmouseover = () => closeBtn.style.opacity = '1';
    closeBtn.onmouseout = () => closeBtn.style.opacity = '0.6';
    closeBtn.onclick = () => this.dismiss();
    this.element.appendChild(closeBtn);

    container.appendChild(this.element);

    // Auto dismiss
    if (this.duration > 0) {
      setTimeout(() => this.dismiss(), this.duration);
    }

    return this.element;
  }

  getTypeStyling() {
    const styles = {
      success: {
        bg: 'rgba(76, 175, 80, 0.95)',
        text: '#fff',
        border: '4px solid #4CAF50'
      },
      error: {
        bg: 'rgba(244, 67, 54, 0.95)',
        text: '#fff',
        border: '4px solid #F44336'
      },
      info: {
        bg: 'rgba(33, 150, 243, 0.95)',
        text: '#fff',
        border: '4px solid #2196F3'
      },
      warning: {
        bg: 'rgba(255, 152, 0, 0.95)',
        text: '#fff',
        border: '4px solid #FF9800'
      }
    };
    return styles[this.type] || styles.info;
  }

  getIcon() {
    const icon = document.createElement('span');
    icon.style.cssText = `
      font-size: 18px;
      flex-shrink: 0;
    `;

    const icons = {
      success: '✅',
      error: '❌',
      info: 'ℹ️',
      warning: '⚠️'
    };
    icon.textContent = icons[this.type] || icons.info;
    return icon;
  }

  dismiss() {
    if (this.element) {
      this.element.style.animation = 'slideOut 0.3s ease-in';
      setTimeout(() => {
        this.element?.remove();
      }, 300);
    }
  }
}

// Global helper functions
export function showToast(message, type = 'info', duration = 3000) {
  return new Toast(message, type, duration);
}

export function showSuccess(message, duration = 3000) {
  return new Toast(message, 'success', duration);
}

export function showError(message, duration = 4000) {
  return new Toast(message, 'error', duration);
}

export function showInfo(message, duration = 3000) {
  return new Toast(message, 'info', duration);
}

export function showWarning(message, duration = 3000) {
  return new Toast(message, 'warning', duration);
}

// Add animations to document
if (!document.getElementById('toast-animations')) {
  const style = document.createElement('style');
  style.id = 'toast-animations';
  style.textContent = `
    @keyframes slideIn {
      from {
        transform: translateX(400px);
        opacity: 0;
      }
      to {
        transform: translateX(0);
        opacity: 1;
      }
    }

    @keyframes slideOut {
      from {
        transform: translateX(0);
        opacity: 1;
      }
      to {
        transform: translateX(400px);
        opacity: 0;
      }
    }
  `;
  document.head.appendChild(style);
}

