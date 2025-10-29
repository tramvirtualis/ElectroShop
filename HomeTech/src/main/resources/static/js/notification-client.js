// ========================================
// WebSocket Notification Client
// ========================================

console.log('📦 notification-client.js loaded');

// Make functions global
window.toggleNotifications = function(event) {
    event && event.stopPropagation && event.stopPropagation();
    const dd = document.getElementById('notificationsDropdown');
    if (!dd) return;
    dd.style.display = (dd.style.display === 'none' || dd.style.display === '') ? 'block' : 'none';
};

window.setNotificationCount = function(n) {
    const badge = document.getElementById('notifyBadge');
    if (!badge) return;
    if (n > 0) {
        badge.textContent = n;
        badge.classList.remove('d-none');
    } else {
        badge.classList.add('d-none');
    }
};

window.addNotification = function(message) {
    console.log('🔔 addNotification called with:', message);
    const list = document.getElementById('notificationsList');
    if (!list) {
        console.warn('notificationsList not found');
        return;
    }
    // Remove placeholder if exists
    if (list.children.length === 1 && list.children[0].textContent.includes('không có thông báo')) {
        list.innerHTML = '';
    }
    const item = document.createElement('div');
    item.style.padding = '0.6rem 0.4rem';
    item.style.borderRadius = '6px';
    item.style.borderBottom = '1px solid rgba(255,255,255,0.05)';
    item.style.background = 'rgba(0,191,255,0.08)';
    item.textContent = message;
    list.prepend(item);
    // increase badge
    let current = 0;
    const badge = document.getElementById('notifyBadge');
    if (badge && !badge.classList.contains('d-none')) {
        current = parseInt(badge.textContent || '0') || 0;
    }
    window.setNotificationCount(current + 1);
    console.log('🔔 Notification added. New count:', current + 1);
};

// Close notifications when clicking outside
document.addEventListener('click', function(e) {
    const dd = document.getElementById('notificationsDropdown');
    const bell = document.getElementById('notifyBell');
    if (!dd || !bell) return;
    if (dd.style.display === 'block') {
        if (!dd.contains(e.target) && !bell.contains(e.target)) {
            dd.style.display = 'none';
        }
    }
});

// ========================================
// WebSocket Connection
// ========================================
function initWebSocket() {
    console.log('🔧 Initializing WebSocket...');
    console.log('🔧 SockJS available:', typeof SockJS !== 'undefined');
    console.log('🔧 Stomp available:', typeof Stomp !== 'undefined');
    
    if (typeof SockJS === 'undefined') {
        console.error('❌ SockJS not loaded!');
        return;
    }
    if (typeof Stomp === 'undefined') {
        console.error('❌ Stomp not loaded!');
        return;
    }
    
    try {
        console.log('🔌 Connecting to WebSocket...');
        const socket = new SockJS('/ws');
        const stompClient = Stomp.over(socket);
        
        stompClient.connect({}, function() {
            console.log('✅ WebSocket connected!');
            
            stompClient.subscribe('/topic/notifications', function(message) {
                console.log('📨 Received notification:', message.body);
                try {
                    const notification = JSON.parse(message.body);
                    const msg = (notification && notification.message) ? notification.message : 'Bạn có thông báo mới!';
                    console.log('📢 Showing notification:', msg);
                    
                    // Show toast popup
                    showToastNotification(msg);
                    
                    // Update bell badge and dropdown
                    if (typeof window.addNotification === 'function') {
                        console.log('🔔 Calling window.addNotification');
                        window.addNotification(msg);
                    }
                } catch (e) {
                    console.error('Error processing notification:', e);
                }
            });
        }, function(error) {
            console.error('❌ WebSocket connection error:', error);
        });
        
    } catch (e) {
        console.error('❌ WebSocket initialization failed:', e);
    }
}

// Toast notification function
function showToastNotification(msg) {
    const toast = document.createElement('div');
    toast.className = 'ws-toast-notification';
    toast.innerHTML = '<i class="fas fa-bell"></i> ' + msg;
    toast.style.cssText = 'position:fixed; top:100px; right:20px; background:rgba(0,191,255,0.95); color:#0a0e27; padding:1rem 1.5rem; border-radius:10px; box-shadow:0 10px 40px rgba(0,191,255,0.5); z-index:9999; animation:slideIn 0.3s ease-out; font-weight:600;';
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.style.animation = 'slideOut 0.3s ease-out';
        setTimeout(() => {
            if (document.body.contains(toast)) {
                document.body.removeChild(toast);
            }
        }, 300);
    }, 4000);
}

// Auto-init when DOM is ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initWebSocket);
} else {
    // DOM already loaded
    initWebSocket();
}

