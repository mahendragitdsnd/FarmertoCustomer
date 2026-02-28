// auth.js

document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');

    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;

            try {
                const response = await fetch('/api/auth/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email, password })
                });

                if (response.ok) {
                    const user = await response.json();
                    localStorage.setItem('user', JSON.stringify(user));
                    alert('Login Successful!');

                    if (user.role === 'FARMER') {
                        window.location.href = 'farmer-dashboard.html';
                    } else {
                        window.location.href = 'index.html'; // Or customer-dashboard
                    }
                } else {
                    alert('Invalid credentials');
                }
            } catch (error) {
                console.error(error);
                alert('Login failed');
            }
        });
    }

    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const name = document.getElementById('name').value;
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const role = document.getElementById('role').value;

            try {
                const response = await fetch('/api/auth/register', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ name, email, password, role })
                });

                if (response.ok) {
                    alert('Registration Successful! Please login.');
                    window.location.href = 'login.html';
                } else {
                    alert('Registration failed');
                }
            } catch (error) {
                console.error(error);
                alert('Error registering');
            }
        });
    }
});

// Logout utility
function logout() {
    localStorage.removeItem('user');
    window.location.href = 'login.html';
}
