import '@testing-library/jest-dom';
import { vi } from 'vitest';

// Mock the Vite env variables
vi.stubGlobal('import.meta', {
    env: { VITE_API_URL: 'http://localhost:8080' }
});