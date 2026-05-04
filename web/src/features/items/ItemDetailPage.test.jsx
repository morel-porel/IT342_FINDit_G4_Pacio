import { render, screen, waitFor } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { AuthContext } from '../auth/AuthContext';
import ItemDetailPage from './ItemDetailPage';
import api from '../../shared/api/api';

vi.mock('../../shared/api/api', () => ({
    default: {
        get: vi.fn(),
        interceptors: { request: { use: vi.fn() } }
    }
}));

const renderWithAuth = (userData, itemId) => {
    return render(
        <MemoryRouter initialEntries={[`/items/${itemId}`]}>
            <AuthContext.Provider value={{ user: userData }}>
                <Routes>
                    <Route path="/items/:id" element={<ItemDetailPage />} />
                </Routes>
            </AuthContext.Provider>
        </MemoryRouter>
    );
};

describe('ItemDetailPage', () => {
    const mockItem = {
        id: 10,
        name: 'Blue Jacket',
        type: 'FOUND',
        status: 'OPEN',
        category: 'Clothing',
        location: 'Library',
        dateLostFound: '2023-10-10',
        reporter: { id: 1, fullName: 'Alice' }
    };

it('shows claim panel for non-owners of a found item', async () => {
        api.get.mockResolvedValue({ data: mockItem });
        renderWithAuth({ id: 2, fullName: 'Bob' }, 10);

        await waitFor(() => {
            expect(screen.getByText(/Is this yours\?/i)).toBeInTheDocument();
            // FIX: Use getByText instead of getByPlaceholderText
            expect(screen.getByText(/Describe how you can prove ownership/i)).toBeInTheDocument();
        });
    });

    it('hides claim panel and shows owner note if user is the reporter', async () => {
        api.get.mockResolvedValue({ data: mockItem });
        renderWithAuth({ id: 1, fullName: 'Alice' }, 10);

        await waitFor(() => {
            // FIX: Use queryByText for "not.toBeInTheDocument" assertions
            expect(screen.queryByText(/Is this yours\?/i)).not.toBeInTheDocument();
            expect(screen.getByText(/This is your report/i)).toBeInTheDocument();
        });
    });
});