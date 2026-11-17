import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider } from './context/ThemeContext';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import ProtectedRoute from './components/ProtectedRoute';
import PublicOnlyRoute from './components/PublicOnlyRoute';
import AppLayout from './components/AppLayout';
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import KnowledgeHubPage from './pages/KnowledgeHubPage';
import DsaTrackerPage from './pages/DsaTrackerPage';
import CareerHubPage from './pages/CareerHubPage';
import ProfilePage from './pages/ProfilePage';

function PublicLayout({ children }) {
  return (
    <div className="min-h-screen bg-bg-page text-text-primary flex flex-col font-sans transition-colors">
      <Navbar />
      <main className="flex-1">{children}</main>
      <Footer />
    </div>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <ThemeProvider>
        <AuthProvider>
          <Routes>
            {/* Public Landing Page */}
            <Route
              path="/"
              element={
                <PublicLayout>
                  <LandingPage />
                </PublicLayout>
              }
            />

            {/* Public-only authentication routes */}
            <Route
              path="/login"
              element={
                <PublicOnlyRoute>
                  <PublicLayout>
                    <LoginPage />
                  </PublicLayout>
                </PublicOnlyRoute>
              }
            />
            <Route
              path="/register"
              element={
                <PublicOnlyRoute>
                  <PublicLayout>
                    <RegisterPage />
                  </PublicLayout>
                </PublicOnlyRoute>
              }
            />

            {/* Protected member workspace routes (with responsive Sidebar) */}
            <Route
              path="/dashboard"
              element={
                <ProtectedRoute>
                  <AppLayout>
                    <DashboardPage />
                  </AppLayout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/knowledge-hub"
              element={
                <ProtectedRoute>
                  <AppLayout>
                    <KnowledgeHubPage />
                  </AppLayout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/dsa"
              element={
                <ProtectedRoute>
                  <AppLayout>
                    <DsaTrackerPage />
                  </AppLayout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/career"
              element={
                <ProtectedRoute>
                  <AppLayout>
                    <CareerHubPage />
                  </AppLayout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/profile"
              element={
                <ProtectedRoute>
                  <AppLayout>
                    <ProfilePage />
                  </AppLayout>
                </ProtectedRoute>
              }
            />

            {/* Fallback route */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </AuthProvider>
      </ThemeProvider>
    </BrowserRouter>
  );
}
