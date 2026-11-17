import React, { useState, useEffect, useCallback } from 'react';
import { AuthContext } from './AuthContextObject';
import {
  getStoredToken,
  setStoredToken,
  removeStoredToken,
  loginApi,
  registerApi,
  fetchCurrentUserApi,
  updateProfileApi,
  logoutApi,
} from '../services/api';

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(getStoredToken());
  const [isLoading, setIsLoading] = useState(true);

  // Restore session on initial application load if token exists
  useEffect(() => {
    let isMounted = true;

    async function restoreSession() {
      const storedToken = getStoredToken();
      if (!storedToken) {
        if (isMounted) {
          setIsLoading(false);
        }
        return;
      }

      try {
        const userData = await fetchCurrentUserApi();
        if (isMounted) {
          setUser(userData);
          setToken(storedToken);
        }
      } catch (err) {
        console.warn('Stored session invalid or expired:', err.message);
        removeStoredToken();
        if (isMounted) {
          setUser(null);
          setToken(null);
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    restoreSession();

    return () => {
      isMounted = false;
    };
  }, []);

  const login = useCallback(async (email, password) => {
    const data = await loginApi(email, password);
    setStoredToken(data.token);
    setToken(data.token);
    setUser(data.user);
    return data.user;
  }, []);

  const register = useCallback(async (fullName, email, password) => {
    const data = await registerApi(fullName, email, password);
    setStoredToken(data.token);
    setToken(data.token);
    setUser(data.user);
    return data.user;
  }, []);

  const logout = useCallback(async () => {
    try {
      await logoutApi();
    } finally {
      removeStoredToken();
      setToken(null);
      setUser(null);
    }
  }, []);

  const updateProfile = useCallback(async (fullName) => {
    const updatedUser = await updateProfileApi(fullName);
    setUser(updatedUser);
    return updatedUser;
  }, []);

  const refreshUser = useCallback(async () => {
    try {
      const userData = await fetchProfileApi();
      setUser(userData);
      return userData;
    } catch (_) {
      return null;
    }
  }, []);

  const updateCurrentUser = useCallback((userData) => {
    setUser(userData);
  }, []);

  const value = {
    user,
    token,
    isAuthenticated: !!user && !!token,
    isLoading,
    login,
    register,
    logout,
    updateProfile,
    refreshUser,
    updateCurrentUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
