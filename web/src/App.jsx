import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import Dashboard from "./pages/Dashboard";
import ProfilePage from "./pages/ProfilePage";
import { useAuth } from "./utils/AuthContext";
import "./App.css";

function App() {
    const { isAuthenticated } = useAuth();
    return (
        <Router>
            <Routes>
                <Route path="/login" element={!isAuthenticated ? <LoginPage /> : <Navigate to="/dashboard" />} />
                <Route path="/register" element={!isAuthenticated ? <RegisterPage /> : <Navigate to="/dashboard" />} />
                <Route path="/dashboard" element={isAuthenticated ? <Dashboard /> : <Navigate to="/login" />} />
                <Route path="/profile" element={isAuthenticated ? <ProfilePage /> : <Navigate to="/login" />} />
                <Route path="/" element={<Navigate to={isAuthenticated ? "/dashboard" : "/login"} />} />
            </Routes>
        </Router>
    );
}

export default App;