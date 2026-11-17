import React from 'react';
import HeroSection from '../components/HeroSection';
import SystemStatusCard from '../components/SystemStatusCard';
import FeatureCards from '../components/FeatureCards';

export default function LandingPage() {
  return (
    <div className="space-y-12 pb-16">
      <HeroSection />
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-12">
        <FeatureCards />
        <SystemStatusCard />
      </div>
    </div>
  );
}
