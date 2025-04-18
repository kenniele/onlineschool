document.addEventListener('DOMContentLoaded', function() {
    // Проверка авторизации
    checkAuthStatus();
});

async function checkAuthStatus() {
    try {
        const response = await fetch('/api/auth/status');
        const data = await response.json();
        
        if (data.authenticated) {
            updateUIForAuthenticatedUser(data.roles);
        } else {
            updateUIForGuest();
        }
    } catch (error) {
        console.error('Ошибка при проверке статуса авторизации:', error);
        updateUIForGuest();
    }
}

function updateUIForAuthenticatedUser(roles) {
    const authLinks = document.getElementById('auth-links');
    authLinks.innerHTML = `
        <a href="/profile" class="profile-btn">Профиль</a>
        <a href="/logout" class="logout-btn">Выйти</a>
    `;

    // Добавляем дополнительные элементы меню в зависимости от ролей
    const navLinks = document.querySelector('.nav-links');
    
    if (roles.includes('ADMIN')) {
        navLinks.innerHTML += `
            <li><a href="/admin">Панель администратора</a></li>
            <li><a href="/users">Управление пользователями</a></li>
        `;
    }
    
    if (roles.includes('TEACHER')) {
        navLinks.innerHTML += `
            <li><a href="/courses">Мои курсы</a></li>
            <li><a href="/materials">Учебные материалы</a></li>
        `;
    }
    
    if (roles.includes('STUDENT')) {
        navLinks.innerHTML += `
            <li><a href="/my-courses">Мои курсы</a></li>
            <li><a href="/progress">Прогресс обучения</a></li>
        `;
    }
}

function updateUIForGuest() {
    const authLinks = document.getElementById('auth-links');
    authLinks.innerHTML = `
        <a href="/login" class="login-btn">Войти</a>
        <a href="/register" class="register-btn">Регистрация</a>
    `;
}

// Обработка выхода из системы
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('logout-btn')) {
        e.preventDefault();
        logout();
    }
});

async function logout() {
    try {
        const response = await fetch('/api/auth/logout', {
            method: 'POST',
            credentials: 'include'
        });
        
        if (response.ok) {
            window.location.href = '/';
        }
    } catch (error) {
        console.error('Ошибка при выходе из системы:', error);
    }
} 