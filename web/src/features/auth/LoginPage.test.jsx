import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import { AuthProvider } from './AuthContext';
import LoginPage from './LoginPage';
import api from '../../shared/api/api';

// Mock the API module
vi.mock('../../shared/api/api', () => ({
    default: {
        post: vi.fn(),
        interceptors: { request: { use: vi.fn() } }
    }
}));

describe('LoginPage', () => {
    it('renders login form and submits data', async () => {
        render(
            <MemoryRouter>
                <AuthProvider>
                    <LoginPage />
                </AuthProvider>
            </MemoryRouter>
        );

        // Check if fields exist
        const emailInput = screen.getByPlaceholderText(/you@university.edu/i);
        const passwordInput = screen.getByPlaceholderText(/••••••••/i);
        const loginButton = screen.getByRole('button', { name: /Login/i });

        // Simulate typing
        fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
        fireEvent.change(passwordInput, { target: { value: 'password123' } });

        // Mock successful API response
        api.post.mockResolvedValue({
            data: { token: 'mock-token', user: { fullName: 'Test User', email: 'test@example.com' } }
        });

        // Click submit
        fireEvent.click(loginButton);

        await waitFor(() => {
            expect(api.post).toHaveBeenCalledWith('/auth/login', {
                email: 'test@example.com',
                password: 'password123'
            });
        });
    });
});