import React, { useState, useEffect } from 'react';
import Card, { CardHeader, CardTitle, CardContent } from '../ui/Card';
import Badge from '../ui/Badge';
import Button from '../ui/Button';
import Input from '../ui/Input';
import Modal from '../ui/Modal';

export default function AssessmentsTab({
  assessments,
  attemptHistory,
  onGenerateAssessment,
  onStartAssessment,
  activeDelivery,
  onSubmitAssessment,
  lastResult,
  onClearDeliveryAndResult,
}) {
  const [generateModalOpen, setGenerateModalOpen] = useState(false);

  // Generate Quiz Form
  const [generateForm, setGenerateForm] = useState({
    title: '',
    topic: 'Java & Object-Oriented Design',
    assessmentType: 'TOPIC_QUIZ',
    difficulty: 'INTERMEDIATE',
    timeLimitMinutes: 15,
    numberOfQuestions: 5,
  });

  // Test-Taking State
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [userAnswers, setUserAnswers] = useState({});
  const [secondsRemaining, setSecondsRemaining] = useState(0);

  useEffect(() => {
    if (activeDelivery) {
      setCurrentQuestionIndex(0);
      setUserAnswers({});
      setSecondsRemaining((activeDelivery.timeLimitMinutes || 15) * 60);
    }
  }, [activeDelivery]);

  // Countdown timer
  useEffect(() => {
    if (!activeDelivery || secondsRemaining <= 0) return;
    const interval = setInterval(() => {
      setSecondsRemaining((prev) => {
        if (prev <= 1) {
          clearInterval(interval);
          handleAutoSubmit();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
    return () => clearInterval(interval);
  }, [activeDelivery, secondsRemaining]);

  const handleSelectAnswer = (questionId, option) => {
    setUserAnswers((prev) => ({
      ...prev,
      [questionId]: option,
    }));
  };

  const handleAutoSubmit = () => {
    if (activeDelivery) {
      onSubmitAssessment(activeDelivery.id, {
        userAnswers,
        timeSpentSeconds: (activeDelivery.timeLimitMinutes * 60) - secondsRemaining,
      });
    }
  };

  const handleManualSubmit = (e) => {
    e.preventDefault();
    if (activeDelivery) {
      onSubmitAssessment(activeDelivery.id, {
        userAnswers,
        timeSpentSeconds: (activeDelivery.timeLimitMinutes * 60) - secondsRemaining,
      });
    }
  };

  const handleGenerateSubmit = (e) => {
    e.preventDefault();
    const finalTitle =
      generateForm.title && generateForm.title.trim()
        ? generateForm.title.trim()
        : `${generateForm.topic} ${generateForm.difficulty} Quiz`;
    onGenerateAssessment({
      ...generateForm,
      title: finalTitle,
    });
    setGenerateModalOpen(false);
  };

  const formatTimer = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs < 10 ? '0' : ''}${secs}`;
  };

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-bg-surface border border-border-default rounded-2xl p-5 shadow-sm">
        <div>
          <h2 className="text-lg font-bold text-text-primary tracking-tight">Assessment & Quiz Studio</h2>
          <p className="text-xs text-text-muted mt-0.5">
            Diagnostic and role-readiness assessments with zero answer leakage and granular explanation breakdowns.
          </p>
        </div>
        <Button
          variant="primary"
          onClick={() => setGenerateModalOpen(true)}
          className="bg-brand-teal text-white shadow-sm text-xs self-start sm:self-auto"
        >
          + Generate Assessment
        </Button>
      </div>

      {/* Main Grid: Available Quizzes & Past Attempt History */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left: Available Assessments */}
        <div className="lg:col-span-7 space-y-4">
          <h3 className="text-xs font-bold uppercase tracking-wider text-text-muted px-1">
            Available Assessments ({assessments?.length || 0})
          </h3>
          {assessments && assessments.length > 0 ? (
            <div className="space-y-3">
              {assessments.map((quiz) => (
                <div
                  key={quiz.id}
                  className="bg-bg-surface border border-border-default rounded-2xl p-5 shadow-sm hover:border-brand-teal/40 transition-all space-y-3"
                >
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <div className="flex items-center gap-2 flex-wrap">
                        <span className="text-[10px] font-bold px-2 py-0.5 rounded uppercase bg-brand-teal/15 text-brand-teal">
                          {quiz.topic}
                        </span>
                        <Badge
                          variant={
                            quiz.difficulty === 'ADVANCED'
                              ? 'error'
                              : quiz.difficulty === 'INTERMEDIATE'
                              ? 'warning'
                              : 'success'
                          }
                          size="sm"
                        >
                          {quiz.difficulty}
                        </Badge>
                      </div>
                      <h4 className="text-sm sm:text-base font-bold text-text-primary mt-1.5">{quiz.title}</h4>
                      <p className="text-xs text-text-muted mt-0.5">{quiz.description}</p>
                    </div>

                    <Button
                      variant="primary"
                      size="sm"
                      onClick={() => onStartAssessment(quiz.id)}
                      className="bg-brand-teal text-white text-xs shrink-0"
                    >
                      Take Quiz →
                    </Button>
                  </div>

                  <div className="flex items-center justify-between text-xs text-text-muted pt-3 border-t border-border-subtle">
                    <span>{quiz.questionCount} Questions</span>
                    <span>⏱ {quiz.timeLimitMinutes} mins limit</span>
                    <span>Pass: {quiz.passingScorePercentage}%</span>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="p-8 text-center text-xs text-text-muted bg-bg-surface border border-border-default rounded-2xl">
              No assessments created yet. Click "+ Generate Assessment" to start.
            </div>
          )}
        </div>

        {/* Right: Past Attempts History Table */}
        <div className="lg:col-span-5 space-y-4">
          <h3 className="text-xs font-bold uppercase tracking-wider text-text-muted px-1">
            Attempt History ({attemptHistory?.length || 0})
          </h3>
          <Card>
            <CardContent className="p-0">
              {attemptHistory && attemptHistory.length > 0 ? (
                <div className="divide-y divide-border-subtle max-h-[500px] overflow-y-auto">
                  {attemptHistory.map((att) => (
                    <div key={att.id} className="p-4 hover:bg-bg-surface-elevated/40 transition-colors space-y-1">
                      <div className="flex items-center justify-between">
                        <span className="text-xs font-bold text-text-primary truncate max-w-[200px]">
                          {att.assessmentTitle}
                        </span>
                        <Badge variant={att.passed ? 'success' : 'error'} size="sm">
                          {att.scorePercentage}%
                        </Badge>
                      </div>
                      <div className="flex items-center justify-between text-[11px] text-text-muted">
                        <span>{att.topic}</span>
                        <span>{att.completedAt?.split('T')[0]}</span>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="p-8 text-center text-xs text-text-muted">
                  No assessment attempts recorded yet.
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>

      {/* Modal: Interactive Test-Taking Environment */}
      {activeDelivery && (
        <Modal
          isOpen={!!activeDelivery}
          onClose={() => {
            if (window.confirm('Are you sure you want to abandon this assessment? Your progress will not be saved.')) {
              onClearDeliveryAndResult();
            }
          }}
          title={activeDelivery.title}
          subtitle={`Question ${currentQuestionIndex + 1} of ${activeDelivery.questions?.length || 0} • Topic: ${activeDelivery.topic}`}
          maxWidth="max-w-3xl"
        >
          <div className="space-y-6">
            {/* Countdown Timer Bar */}
            <div className="flex items-center justify-between p-3 rounded-xl bg-bg-surface-elevated border border-border-subtle">
              <span className="text-xs text-text-muted font-medium">Time Remaining</span>
              <span
                className={`text-sm font-extrabold font-mono ${
                  secondsRemaining < 120 ? 'text-rose-500 animate-pulse' : 'text-brand-teal'
                }`}
              >
                ⏱ {formatTimer(secondsRemaining)}
              </span>
            </div>

            {/* Question Card */}
            {activeDelivery.questions && activeDelivery.questions[currentQuestionIndex] && (
              <div className="space-y-4">
                <div className="p-4 rounded-xl bg-bg-surface border border-border-default space-y-2">
                  <div className="flex items-center justify-between text-xs text-text-muted">
                    <span className="font-semibold text-brand-gold">
                      Subtopic: {activeDelivery.questions[currentQuestionIndex].subtopic || 'Core'}
                    </span>
                    <span>Points: {activeDelivery.questions[currentQuestionIndex].points}</span>
                  </div>
                  <h3 className="text-sm sm:text-base font-bold text-text-primary leading-snug">
                    {activeDelivery.questions[currentQuestionIndex].questionText}
                  </h3>
                </div>

                {/* Options List */}
                <div className="space-y-2.5">
                  {activeDelivery.questions[currentQuestionIndex].options?.map((option, optIdx) => {
                    const qId = activeDelivery.questions[currentQuestionIndex].id;
                    const isSelected = userAnswers[qId] === option;
                    return (
                      <div
                        key={optIdx}
                        onClick={() => handleSelectAnswer(qId, option)}
                        className={`p-3.5 rounded-xl border cursor-pointer transition-all flex items-center gap-3 ${
                          isSelected
                            ? 'bg-brand-teal/10 border-brand-teal ring-1 ring-brand-teal font-medium'
                            : 'bg-bg-surface border-border-default hover:border-brand-teal/30 hover:bg-bg-surface-elevated/40'
                        }`}
                      >
                        <span
                          className={`w-5 h-5 rounded-full border flex items-center justify-center text-xs font-bold shrink-0 ${
                            isSelected
                              ? 'bg-brand-teal border-brand-teal text-white'
                              : 'border-border-default text-text-muted'
                          }`}
                        >
                          {String.fromCharCode(65 + optIdx)}
                        </span>
                        <span className="text-xs sm:text-sm text-text-primary">{option}</span>
                      </div>
                    );
                  })}
                </div>
              </div>
            )}

            {/* Navigation & Submit Buttons */}
            <div className="flex items-center justify-between pt-4 border-t border-border-subtle">
              <Button
                variant="outline"
                size="sm"
                disabled={currentQuestionIndex === 0}
                onClick={() => setCurrentQuestionIndex((prev) => Math.max(0, prev - 1))}
                className="text-xs"
              >
                ← Previous
              </Button>

              <div className="flex items-center gap-2">
                {currentQuestionIndex < (activeDelivery.questions?.length || 1) - 1 ? (
                  <Button
                    variant="primary"
                    size="sm"
                    onClick={() => setCurrentQuestionIndex((prev) => prev + 1)}
                    className="bg-brand-teal text-white text-xs"
                  >
                    Next →
                  </Button>
                ) : (
                  <Button
                    variant="primary"
                    size="sm"
                    onClick={handleManualSubmit}
                    className="bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold shadow-md"
                  >
                    Submit Quiz
                  </Button>
                )}
              </div>
            </div>
          </div>
        </Modal>
      )}

      {/* Modal: Instant Diagnostic Results Screen */}
      {lastResult && (
        <Modal
          isOpen={!!lastResult}
          onClose={onClearDeliveryAndResult}
          title="Assessment Diagnostic Results"
          subtitle={`Topic: ${lastResult.topic} • Score: ${lastResult.scorePercentage}%`}
          maxWidth="max-w-4xl"
        >
          <div className="space-y-6">
            {/* Score & Verdict Banner */}
            <div
              className={`p-6 rounded-2xl border text-center space-y-2 ${
                lastResult.passed
                  ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-600 dark:text-emerald-400'
                  : 'bg-rose-500/10 border-rose-500/30 text-rose-600 dark:text-rose-400'
              }`}
            >
              <div className="text-4xl sm:text-5xl font-black tracking-tight">{lastResult.scorePercentage}%</div>
              <h3 className="text-base sm:text-lg font-bold">
                {lastResult.passed ? '🎉 Congratulations! Assessment Passed' : '⚠️ Assessment Needs Revision'}
              </h3>
              <p className="text-xs text-text-muted">
                Earned {lastResult.earnedPoints} out of {lastResult.totalPoints} total points in {lastResult.timeSpentSeconds || 0}s.
              </p>
            </div>

            {/* Diagnostic Topic Breakdown & Recommended Actions */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="p-4 rounded-xl bg-bg-surface-elevated border border-border-subtle space-y-2">
                <h4 className="text-xs font-bold uppercase tracking-wider text-text-primary">Topic Breakdown</h4>
                {lastResult.topicBreakdown && Object.keys(lastResult.topicBreakdown).length > 0 ? (
                  <div className="space-y-1.5">
                    {Object.entries(lastResult.topicBreakdown).map(([subtopic, score]) => (
                      <div key={subtopic} className="flex justify-between text-xs">
                        <span className="text-text-muted">{subtopic}</span>
                        <span className="font-semibold text-text-primary">{score}%</span>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-xs text-text-muted">No topic breakdowns recorded.</p>
                )}
              </div>

              <div className="p-4 rounded-xl bg-bg-surface-elevated border border-border-subtle space-y-2">
                <h4 className="text-xs font-bold uppercase tracking-wider text-text-primary">Recommended Next Steps</h4>
                <p className="text-xs text-text-secondary leading-relaxed">
                  {lastResult.recommendedNextSteps || 'Review questions below and schedule spaced repetition check-ins.'}
                </p>
                {lastResult.weakAreas && lastResult.weakAreas.length > 0 && (
                  <div className="pt-1">
                    <span className="text-[11px] text-text-muted block">Weak Areas Flagged:</span>
                    <div className="flex gap-1.5 flex-wrap mt-1">
                      {lastResult.weakAreas.map((w, idx) => (
                        <span key={idx} className="text-[10px] px-2 py-0.5 rounded bg-rose-500/15 text-rose-500 font-semibold">
                          {w}
                        </span>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            </div>

            {/* In-Depth Question Reviews with Answers & Explanations */}
            <div className="space-y-4">
              <h4 className="text-sm font-bold text-text-primary">Detailed Answers & Explanations</h4>
              <div className="space-y-3 max-h-[400px] overflow-y-auto pr-1">
                {lastResult.questionResults?.map((q, idx) => (
                  <div
                    key={idx}
                    className={`p-4 rounded-xl border space-y-2 ${
                      q.isCorrect
                        ? 'bg-emerald-500/5 border-emerald-500/20'
                        : 'bg-rose-500/5 border-rose-500/20'
                    }`}
                  >
                    <div className="flex items-start justify-between gap-2">
                      <span className="text-xs font-bold text-text-primary">
                        Q{idx + 1}. {q.questionText}
                      </span>
                      <span className="text-xs shrink-0 font-bold">
                        {q.isCorrect ? '✅ Correct' : '❌ Incorrect'}
                      </span>
                    </div>

                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 text-xs pt-1">
                      <div>
                        <span className="text-text-muted block text-[11px]">Your Answer:</span>
                        <span className={`font-semibold ${q.isCorrect ? 'text-emerald-500' : 'text-rose-500'}`}>
                          {q.userAnswer || '(No answer provided)'}
                        </span>
                      </div>
                      <div>
                        <span className="text-text-muted block text-[11px]">Correct Answer:</span>
                        <span className="font-semibold text-emerald-500">{q.correctAnswer}</span>
                      </div>
                    </div>

                    {q.explanation && (
                      <div className="p-3 rounded-lg bg-bg-surface border border-border-subtle text-xs text-text-secondary leading-relaxed mt-2">
                        <strong className="text-brand-teal font-semibold block mb-0.5">Pedagogical Explanation:</strong>
                        {q.explanation}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </div>

            <div className="flex justify-end pt-3 border-t border-border-subtle">
              <Button variant="primary" onClick={onClearDeliveryAndResult} className="bg-brand-teal text-white text-xs">
                Close Results
              </Button>
            </div>
          </div>
        </Modal>
      )}

      {/* Modal: Generate Assessment */}
      <Modal
        isOpen={generateModalOpen}
        onClose={() => setGenerateModalOpen(false)}
        title="Generate Customized Quiz"
        subtitle="Generates topic-targeted MCQs and diagnostic questions."
      >
        <form onSubmit={handleGenerateSubmit} className="space-y-4">
          <Input
            label="Quiz Title (Optional)"
            value={generateForm.title}
            onChange={(e) => setGenerateForm({ ...generateForm, title: e.target.value })}
            placeholder="e.g. Concurrency & Memory Model Diagnostic"
          />

          <div>
            <label className="block text-xs font-semibold text-text-muted mb-1.5">Assessment Topic</label>
            <select
              value={generateForm.topic}
              onChange={(e) => setGenerateForm({ ...generateForm, topic: e.target.value })}
              className="w-full px-3.5 py-2.5 rounded-xl border border-border-default bg-bg-surface text-text-primary text-xs focus:outline-none focus:ring-2 focus:ring-brand-teal"
            >
              <option value="Java & Object-Oriented Design">Java & Object-Oriented Design</option>
              <option value="Data Structures & Algorithms">Data Structures & Algorithms</option>
              <option value="SQL & Relational Databases">SQL & Relational Databases</option>
              <option value="System Design & Architecture">System Design & Architecture</option>
              <option value="Cloud & Microservices">Cloud & Microservices</option>
            </select>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-text-muted mb-1.5">Difficulty Level</label>
              <select
                value={generateForm.difficulty}
                onChange={(e) => setGenerateForm({ ...generateForm, difficulty: e.target.value })}
                className="w-full px-3.5 py-2.5 rounded-xl border border-border-default bg-bg-surface text-text-primary text-xs focus:outline-none focus:ring-2 focus:ring-brand-teal"
              >
                <option value="BEGINNER">Beginner (Foundations)</option>
                <option value="INTERMEDIATE">Intermediate (Core Concepts)</option>
                <option value="ADVANCED">Advanced (Production Invariants)</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-semibold text-text-muted mb-1.5">Question Count</label>
              <select
                value={generateForm.numberOfQuestions}
                onChange={(e) => setGenerateForm({ ...generateForm, numberOfQuestions: parseInt(e.target.value) || 5 })}
                className="w-full px-3.5 py-2.5 rounded-xl border border-border-default bg-bg-surface text-text-primary text-xs focus:outline-none focus:ring-2 focus:ring-brand-teal"
              >
                <option value={5}>5 Questions (Quick Check)</option>
                <option value={10}>10 Questions (Standard Quiz)</option>
                <option value={15}>15 Questions (Full Diagnostic)</option>
              </select>
            </div>
          </div>

          <div className="flex justify-end gap-3 pt-3 border-t border-border-subtle">
            <Button variant="outline" type="button" onClick={() => setGenerateModalOpen(false)}>
              Cancel
            </Button>
            <Button variant="primary" type="submit" className="bg-brand-teal text-white">
              Generate & Launch
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
