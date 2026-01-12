export default defineNuxtRouteMiddleware(async (to, from) => {
    const token = useCookie('auth_token');

    if (!token.value) {
        return navigateTo('/user/login');
    }

    try {
        // Verify token with backend
        await $fetch('/api/auth/check', {
            headers: {
                Authorization: `Bearer ${token.value}`
            }
        });
    } catch (error) {
        // If verification fails, clear token and redirect
        token.value = null;
        return navigateTo('/user/login');
    }
});
