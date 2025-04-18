// Обработка форм аутентификации
document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(loginForm);
            
            try {
                const response = await fetch('/api/auth/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        username: formData.get('username'),
                        password: formData.get('password')
                    })
                });
                
                if (response.ok) {
                    const data = await response.json();
                    localStorage.setItem('token', data.token);
                    window.location.href = '/dashboard.html';
                } else {
                    const error = await response.json();
                    showError(error.message);
                }
            } catch (error) {
                showError('Ошибка при входе в систему');
            }
        });
    }
    
    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(registerForm);
            
            try {
                const response = await fetch('/api/auth/register', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        username: formData.get('username'),
                        email: formData.get('email'),
                        password: formData.get('password'),
                        role: 'STUDENT'
                    })
                });
                
                if (response.ok) {
                    window.location.href = '/login.html';
                } else {
                    const error = await response.json();
                    showError(error.message);
                }
            } catch (error) {
                showError('Ошибка при регистрации');
            }
        });
    }
});

// Проверка статуса аутентификации
async function checkAuth() {
    const token = localStorage.getItem('token');
    if (!token) return false;
    
    try {
        const response = await fetch('/api/auth/me', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const user = await response.json();
            updateUI(user);
            return true;
        }
    } catch (error) {
        console.error('Ошибка проверки аутентификации:', error);
    }
    
    return false;
}

// Обновление UI в зависимости от статуса аутентификации
function updateUI(user) {
    const navLinks = document.querySelector('.nav-links');
    const authButtons = document.querySelector('.auth-buttons');
    
    if (user) {
        authButtons.innerHTML = `
            <span>${user.username}</span>
            <button onclick="logout()">Выйти</button>
        `;
        
        if (user.role === 'TEACHER') {
            navLinks.innerHTML += `
                <a href="/my-webinars.html">Мои вебинары</a>
                <a href="/create-webinar.html">Создать вебинар</a>
            `;
        }
    }
}

// Выход из системы
function logout() {
    localStorage.removeItem('token');
    window.location.href = '/';
}

// Показать сообщение об ошибке
function showError(message) {
    const errorDiv = document.createElement('div');
    errorDiv.className = 'error-message';
    errorDiv.textContent = message;
    
    document.body.appendChild(errorDiv);
    setTimeout(() => errorDiv.remove(), 3000);
}

// Инициализация при загрузке страницы
checkAuth(); 