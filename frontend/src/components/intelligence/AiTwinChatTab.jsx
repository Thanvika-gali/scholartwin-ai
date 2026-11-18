import React, { useState, useRef, useEffect } from 'react';
import Card, { CardHeader, CardTitle, CardContent } from '../ui/Card';
import Button from '../ui/Button';
import Badge from '../ui/Badge';

export default function AiTwinChatTab({
  chatHistory = [],
  isLoading,
  onSendMessage,
  onClearHistory,
}) {
  const [inputText, setInputText] = useState('');
  const [includeGrounding, setIncludeGrounding] = useState(true);
  const [expandedMemories, setExpandedMemories] = useState(new Set());
  const messagesEndRef = useRef(null);

  const promptStarters = [
    {
      title: 'Active Weak Area Revision',
      prompt: 'Based on my recent study sessions and memory records, what weak topics should I revise today?',
    },
    {
      title: 'DSA vs Career Readiness',
      prompt: 'Compare my DSA progress against my target role and identify which topics need more practice.',
    },
    {
      title: 'Synthesize Vault Notes',
      prompt: 'Summarize the core takeaways from my saved notes and documents in the Knowledge Vault.',
    },
    {
      title: 'Update Learning Preference',
      prompt: 'Remember that I am preparing for software engineering interviews and prefer concise Java 17 solutions.',
    },
  ];

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [chatHistory, isLoading]);

  const handleSubmit = (e) => {
    e?.preventDefault();
    if (!inputText.trim() || isLoading) return;
    const msg = inputText.trim();
    setInputText('');
    onSendMessage(msg, includeGrounding);
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSubmit();
    }
  };

  const toggleMemoryExpansion = (index) => {
    setExpandedMemories((prev) => {
      const next = new Set(prev);
      if (next.has(index)) next.delete(index);
      else next.add(index);
      return next;
    });
  };

  return (
    <div className="flex flex-col h-[700px] bg-bg-surface border border-border-default rounded-2xl overflow-hidden shadow-sm">
      {/* Chat Header Bar */}
      <div className="p-4 bg-bg-surface border-b border-border-default flex items-center justify-between shrink-0">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-primary-500/10 border border-primary-500/20 flex items-center justify-center text-primary-400 font-bold">
            🤖
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-sm font-bold text-text-primary">AI Twin Academic Co-pilot</h3>
              <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 flex items-center gap-1">
                <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" />
                gemini-1.5-flash
              </span>
            </div>
            <p className="text-[11px] text-text-muted">
              Personalized with dynamic memory grounding across your academic profile.
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {/* Grounding Toggle */}
          <label className="flex items-center gap-1.5 text-xs text-text-secondary cursor-pointer select-none bg-bg-surface border border-border-default rounded-lg px-2.5 py-1">
            <input
              type="checkbox"
              checked={includeGrounding}
              onChange={(e) => setIncludeGrounding(e.target.checked)}
              className="accent-primary-500 rounded"
            />
            <span>Memory Grounding</span>
          </label>

          {/* Clear history */}
          {chatHistory.length > 0 && (
            <Button
              variant="ghost"
              size="xs"
              onClick={onClearHistory}
              title="Clear active conversation"
            >
              Clear
            </Button>
          )}
        </div>
      </div>

      {/* Messages Feed */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4">
        {chatHistory.length === 0 ? (
          <div className="h-full flex flex-col items-center justify-center text-center p-6 space-y-6">
            <div className="max-w-md space-y-2">
              <div className="w-14 h-14 rounded-2xl bg-primary-500/10 text-primary-400 border border-primary-500/20 flex items-center justify-center mx-auto text-2xl">
                🎓
              </div>
              <h4 className="text-base font-bold text-text-primary">Welcome to your AI Twin</h4>
              <p className="text-xs text-text-muted leading-relaxed">
                I remember your course goals, weak topics, DSA performance, and career aspirations. How can I assist your study session today?
              </p>
            </div>

            {/* Quick Starters */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5 max-w-xl w-full">
              {promptStarters.map((starter, i) => (
                <button
                  key={i}
                  onClick={() => {
                    setInputText(starter.prompt);
                  }}
                  className="p-3 text-left rounded-xl bg-bg-surface border border-border-default/80 hover:border-primary-500/40 hover:bg-primary-500/5 transition-all group"
                >
                  <span className="text-xs font-semibold text-text-primary group-hover:text-primary-400 block mb-1">
                    {starter.title} →
                  </span>
                  <span className="text-[11px] text-text-muted line-clamp-2">
                    "{starter.prompt}"
                  </span>
                </button>
              ))}
            </div>
          </div>
        ) : (
          chatHistory.map((msg, index) => {
            const isUser = msg.sender === 'USER';
            const isExpanded = expandedMemories.has(index);

            return (
              <div
                key={msg.id || index}
                className={`flex gap-3 ${isUser ? 'justify-end' : 'justify-start'}`}
              >
                {!isUser && (
                  <div className="w-8 h-8 rounded-xl bg-primary-500/10 border border-primary-500/20 flex items-center justify-center text-primary-400 text-xs font-bold shrink-0 mt-1">
                    🤖
                  </div>
                )}

                <div
                  className={`max-w-[85%] sm:max-w-[75%] rounded-2xl p-4 text-xs space-y-2 leading-relaxed ${
                    isUser
                      ? 'bg-primary-600 text-white rounded-tr-xs shadow-xs'
                      : 'bg-bg-surface border border-border-default text-text-primary rounded-tl-xs shadow-xs'
                  }`}
                >
                  {/* Message content */}
                  <div className="whitespace-pre-wrap">{msg.message}</div>

                  {/* Grounded Memories Disclosure (for Assistant) */}
                  {!isUser && msg.groundedMemories && msg.groundedMemories.length > 0 && (
                    <div className="pt-2 border-t border-border-default/60">
                      <button
                        onClick={() => toggleMemoryExpansion(index)}
                        className="text-[10px] font-mono font-medium text-primary-400 hover:text-primary-300 flex items-center gap-1.5 transition-colors"
                      >
                        <span>💡 Grounded in {msg.groundedMemories.length} profile memories</span>
                        <span>{isExpanded ? '▲' : '▼'}</span>
                      </button>

                      {isExpanded && (
                        <div className="mt-2 space-y-1.5 pl-2 border-l-2 border-primary-500/40">
                          {msg.groundedMemories.map((m, mIdx) => (
                            <div key={mIdx} className="text-[11px] text-text-secondary bg-bg-surface/80 p-2 rounded-lg border border-border-default/60">
                              <span className="text-[9px] font-bold text-primary-400 uppercase tracking-wider block font-mono">
                                {m.category} • {m.type}
                              </span>
                              <span>{m.content}</span>
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  )}

                  {/* Sources / Citations */}
                  {!isUser && msg.sources && msg.sources.length > 0 && (
                    <div className="flex flex-wrap gap-1 pt-1.5">
                      {msg.sources.map((src, sIdx) => (
                        <span
                          key={sIdx}
                          className="text-[9px] px-2 py-0.5 rounded bg-border-default/40 text-text-muted font-mono"
                        >
                          📄 {src}
                        </span>
                      ))}
                    </div>
                  )}

                  {/* Timestamp */}
                  <div
                    className={`text-[9px] font-mono text-right ${
                      isUser ? 'text-white/70' : 'text-text-muted'
                    }`}
                  >
                    {msg.createdAt
                      ? new Date(msg.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
                      : 'Just now'}
                  </div>
                </div>

                {isUser && (
                  <div className="w-8 h-8 rounded-xl bg-border-default/60 border border-border-default flex items-center justify-center text-text-secondary text-xs font-bold shrink-0 mt-1">
                    👤
                  </div>
                )}
              </div>
            );
          })
        )}

        {/* Loading Bubble */}
        {isLoading && (
          <div className="flex gap-3 justify-start">
            <div className="w-8 h-8 rounded-xl bg-primary-500/10 border border-primary-500/20 flex items-center justify-center text-primary-400 text-xs font-bold shrink-0 mt-1">
              🤖
            </div>
            <div className="bg-bg-surface border border-border-default rounded-2xl rounded-tl-xs p-4 flex items-center gap-2 text-xs text-text-muted">
              <span className="w-2 h-2 rounded-full bg-primary-500 animate-bounce" />
              <span className="w-2 h-2 rounded-full bg-primary-500 animate-bounce [animation-delay:0.2s]" />
              <span className="w-2 h-2 rounded-full bg-primary-500 animate-bounce [animation-delay:0.4s]" />
              <span className="text-[11px] font-mono ml-2">Retrieving memory context & reasoning...</span>
            </div>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* Input Box Footer */}
      <div className="p-4 bg-bg-surface border-t border-border-default shrink-0">
        <form onSubmit={handleSubmit} className="flex items-end gap-2">
          <div className="flex-1 relative">
            <textarea
              rows={2}
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Ask your AI Twin anything about your studies, DSA, career, or vault notes... (Shift+Enter for new line)"
              className="w-full resize-none text-xs bg-bg-surface border border-border-default rounded-xl p-3 pr-10 text-text-primary placeholder:text-text-muted focus:outline-none focus:border-primary-500"
            />
          </div>

          <Button
            type="submit"
            variant="primary"
            disabled={!inputText.trim() || isLoading}
            className="h-10 px-4"
          >
            Send ➔
          </Button>
        </form>
      </div>
    </div>
  );
}
