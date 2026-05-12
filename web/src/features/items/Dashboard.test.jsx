import { render, screen, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import { AuthProvider } from '../auth/AuthContext';
import Dashboard from './Dashboard';
import api from '../../shared/api/api';

vi.mock('../../shared/api/api', () => ({
    default: {
        get: vi.fn(),
        interceptors: { request: { use: vi.fn() } }
    }
}));

describe('Dashboard', () => {
    const mockItems = [
        { id: 1, name: 'Lost Umbrella', category: 'Other', type: 'LOST', status: 'OPEN', location: 'Gym', dateLostFound: '2023-10-10' },
        { id: 2, name: 'Found Wallet', category: 'Wallet / Bag', type: 'FOUND', status: 'OPEN', location: 'Canteen', dateLostFound: '2023-10-11' }
    ];

    it('renders a list of items from the API', async () => {
        api.get.mockResolvedValue({ data: mockItems });

        render(
            <MemoryRouter>
                <AuthProvider>
                    <Dashboard />
                </AuthProvider>
            </MemoryRouter>
        );

        // Wait for items to appear
        await waitFor(() => {
            expect(screen.getByText('Lost Umbrella')).toBeInTheDocument();
            expect(screen.getByText('Found Wallet')).toBeInTheDocument();
        });

        // Check if badges are rendered
        expect(screen.getAllByText('Lost').length).toBeGreaterThan(0);
        expect(screen.getAllByText('Found').length).toBeGreaterThan(0);
    });
});