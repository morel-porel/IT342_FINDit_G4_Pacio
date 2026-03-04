import { createContext, useState, useContext } from "react";

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [isAuthenticated, setIsAuthenticated] = useState(!!localStorage.getItem("token"));
    const [user, setUser] = useState(JSON.parse(localStorage.getItem("user")));

    const login = (token, userData) => {
        localStorage.setItem("token", token);
        localStorage.setItem("user", JSON.stringify(userData));
        setUser(userData);
        setIsAuthenticated(true);
    };

    const logout = () => {
        localStorage.removeItem("token");
        localStorage.removeItem("user");
        setUser(null);
        setIsAuthenticated(false);
    };

    const updateUser = (userData) => {
        localStorage.setItem("user", JSON.stringify(userData));
        setUser(userData);
    };

    return (
        <AuthContext.Provider value={{ isAuthenticated, user, setUser: updateUser, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);