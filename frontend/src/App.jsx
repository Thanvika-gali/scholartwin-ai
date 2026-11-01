import React from 'react';
import Navbar from './components/Navbar';
import HeroSection from './components/HeroSection';
import FeatureCards from './components/FeatureCards';
import SystemStatusCard from './components/SystemStatusCard';
import Footer from './components/Footer';

export default function App() {
  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans selection:bg-indigo-500/30 selection:text-indigo-200">
      <Navbar />
      <main className="flex-1">
        <HeroSection />
        <SystemStatusCard />
        <FeatureCards />
      </main>
      <Footer />
    </div>
  );
}
