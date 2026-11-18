import React, { useState, useMemo, useRef } from 'react';
import Card, { CardHeader, CardTitle, CardContent } from '../ui/Card';
import Button from '../ui/Button';
import Badge from '../ui/Badge';

export default function KnowledgeGraphTab({
  graphData = { nodes: [], relationships: [] },
  isLoading,
  onSyncGraph,
}) {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedType, setSelectedType] = useState('ALL');
  const [selectedNode, setSelectedNode] = useState(null);
  const [zoomLevel, setZoomLevel] = useState(1);
  const [svgOffset, setSvgOffset] = useState({ x: 0, y: 0 });
  const [isDragging, setIsDragging] = useState(false);
  const dragStartRef = useRef({ x: 0, y: 0 });

  const nodeTypes = [
    { value: 'ALL', label: 'All Entities' },
    { value: 'SKILL', label: 'Skills' },
    { value: 'DSA_TOPIC', label: 'DSA Topics' },
    { value: 'CAREER_GOAL', label: 'Career Goals' },
    { value: 'DOCUMENT', label: 'Documents' },
    { value: 'LEARNING_TASK', label: 'Learning Tasks' },
    { value: 'TOPIC', label: 'Topics' },
  ];

  const getNodeColor = (type) => {
    switch (type) {
      case 'SKILL':
        return { fill: '#178582', stroke: '#0e5553', text: '#ffffff', label: 'Skill' };
      case 'DSA_TOPIC':
        return { fill: '#bfa181', stroke: '#8c7256', text: '#0a1828', label: 'DSA Topic' };
      case 'CAREER_GOAL':
        return { fill: '#3b82f6', stroke: '#1d4ed8', text: '#ffffff', label: 'Career Goal' };
      case 'DOCUMENT':
        return { fill: '#8b5cf6', stroke: '#6d28d9', text: '#ffffff', label: 'Document' };
      case 'LEARNING_TASK':
        return { fill: '#10b981', stroke: '#047857', text: '#ffffff', label: 'Task' };
      default:
        return { fill: '#64748b', stroke: '#334155', text: '#ffffff', label: 'Topic' };
    }
  };

  // Filter nodes
  const filteredNodes = useMemo(() => {
    return (graphData.nodes || []).filter((n) => {
      const matchesSearch = !searchTerm || n.label.toLowerCase().includes(searchTerm.toLowerCase());
      const matchesType = selectedType === 'ALL' || n.nodeType === selectedType;
      return matchesSearch && matchesType;
    });
  }, [graphData.nodes, searchTerm, selectedType]);

  const activeNodeIds = useMemo(() => {
    return new Set(filteredNodes.map((n) => n.id));
  }, [filteredNodes]);

  // Filter relationships connecting visible nodes
  const visibleRelationships = useMemo(() => {
    return (graphData.relationships || []).filter(
      (rel) => activeNodeIds.has(rel.sourceNodeId) && activeNodeIds.has(rel.targetNodeId)
    );
  }, [graphData.relationships, activeNodeIds]);

  // Compute 2D coordinates for nodes (deterministic circular / clustered layout)
  const nodePositions = useMemo(() => {
    const positions = new Map();
    const count = filteredNodes.length;
    if (count === 0) return positions;

    const width = 800;
    const height = 520;
    const centerX = width / 2;
    const centerY = height / 2;
    const radius = Math.min(centerX, centerY) * 0.75;

    filteredNodes.forEach((node, index) => {
      const angle = (index / count) * 2 * Math.PI;
      const x = centerX + radius * Math.cos(angle);
      const y = centerY + radius * Math.sin(angle);
      positions.set(node.id, { x, y });
    });

    return positions;
  }, [filteredNodes]);

  // Pan controls
  const handleMouseDown = (e) => {
    if (e.target.tagName === 'svg' || e.target.tagName === 'g') {
      setIsDragging(true);
      dragStartRef.current = { x: e.clientX - svgOffset.x, y: e.clientY - svgOffset.y };
    }
  };

  const handleMouseMove = (e) => {
    if (!isDragging) return;
    setSvgOffset({
      x: e.clientX - dragStartRef.current.x,
      y: e.clientY - dragStartRef.current.y,
    });
  };

  const handleMouseUp = () => {
    setIsDragging(false);
  };

  // Connected edges for the selected node
  const selectedNodeConnections = useMemo(() => {
    if (!selectedNode) return { incoming: [], outgoing: [] };

    const incoming = (graphData.relationships || [])
      .filter((r) => r.targetNodeId === selectedNode.id)
      .map((r) => {
        const source = (graphData.nodes || []).find((n) => n.id === r.sourceNodeId);
        return { rel: r, node: source };
      })
      .filter((item) => !!item.node);

    const outgoing = (graphData.relationships || [])
      .filter((r) => r.sourceNodeId === selectedNode.id)
      .map((r) => {
        const target = (graphData.nodes || []).find((n) => n.id === r.targetNodeId);
        return { rel: r, node: target };
      })
      .filter((item) => !!item.node);

    return { incoming, outgoing };
  }, [selectedNode, graphData]);

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-bg-surface border border-border-default rounded-2xl p-5 shadow-sm">
        <div>
          <h2 className="text-lg font-bold text-text-primary tracking-tight">Academic Knowledge Graph</h2>
          <p className="text-xs text-text-muted mt-0.5">
            Interactive topological map connecting your curriculum skills, DSA algorithms, uploaded research papers, and career targets.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Button
            variant="primary"
            size="sm"
            onClick={onSyncGraph}
            loading={isLoading}
          >
            ↻ Sync Graph from Activity
          </Button>
        </div>
      </div>

      {/* Control & Filter Bar */}
      <div className="flex flex-wrap items-center justify-between gap-3 bg-bg-surface/50 border border-border-default rounded-xl p-3">
        {/* Search */}
        <div className="flex items-center gap-2 flex-1 min-w-[200px] max-w-sm">
          <span className="text-text-muted text-xs">🔍</span>
          <input
            type="text"
            placeholder="Search entities in graph..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full text-xs bg-bg-surface border border-border-default rounded-lg px-2.5 py-1 text-text-primary placeholder:text-text-muted focus:outline-none focus:border-primary-500"
          />
        </div>

        {/* Entity Type Filter */}
        <div className="flex flex-wrap items-center gap-1.5">
          {nodeTypes.map((type) => (
            <button
              key={type.value}
              onClick={() => setSelectedType(type.value)}
              className={`text-xs px-2.5 py-1 rounded-lg border font-medium transition-all ${
                selectedType === type.value
                  ? 'bg-primary-500/15 border-primary-500/40 text-primary-400 shadow-xs'
                  : 'bg-bg-surface border-border-default/60 text-text-muted hover:text-text-primary hover:border-border-default'
              }`}
            >
              {type.label}
            </button>
          ))}
        </div>

        {/* Zoom controls */}
        <div className="flex items-center gap-1 bg-bg-surface border border-border-default rounded-lg p-0.5">
          <button
            onClick={() => setZoomLevel((z) => Math.min(z + 0.15, 2.5))}
            className="px-2 py-0.5 text-xs text-text-secondary hover:text-text-primary hover:bg-border-default/30 rounded"
            title="Zoom In"
          >
            +
          </button>
          <span className="text-[11px] font-mono text-text-muted px-1.5">
            {Math.round(zoomLevel * 100)}%
          </span>
          <button
            onClick={() => setZoomLevel((z) => Math.max(z - 0.15, 0.4))}
            className="px-2 py-0.5 text-xs text-text-secondary hover:text-text-primary hover:bg-border-default/30 rounded"
            title="Zoom Out"
          >
            -
          </button>
          <button
            onClick={() => {
              setZoomLevel(1);
              setSvgOffset({ x: 0, y: 0 });
            }}
            className="px-2 py-0.5 text-xs text-text-muted hover:text-text-primary hover:bg-border-default/30 rounded"
            title="Reset Zoom & Pan"
          >
            ⟲
          </button>
        </div>
      </div>

      {/* Main Canvas + Detail Drawer */}
      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6 items-start">
        {/* SVG Canvas */}
        <div className="lg:col-span-3 bg-[#07111e] border border-border-default rounded-2xl overflow-hidden shadow-inner relative h-[560px] flex items-center justify-center select-none">
          {filteredNodes.length === 0 ? (
            <div className="text-center p-8 max-w-md">
              <div className="w-12 h-12 rounded-2xl bg-primary-500/10 text-primary-400 border border-primary-500/20 flex items-center justify-center mx-auto mb-3 text-xl">
                🕸
              </div>
              <h3 className="text-base font-semibold text-text-primary">Knowledge Graph Empty</h3>
              <p className="text-xs text-text-muted mt-1">
                No entities mapped yet. Click "Sync Graph from Activity" above to parse your current skills, DSA solutions, notes, and academic tasks into nodes and connections.
              </p>
              <Button
                variant="primary"
                size="sm"
                className="mt-4"
                onClick={onSyncGraph}
                loading={isLoading}
              >
                Sync Knowledge Graph Now
              </Button>
            </div>
          ) : (
            <svg
              className="w-full h-full cursor-grab active:cursor-grabbing"
              viewBox="0 0 800 520"
              onMouseDown={handleMouseDown}
              onMouseMove={handleMouseMove}
              onMouseUp={handleMouseUp}
            >
              <defs>
                <marker
                  id="arrow"
                  viewBox="0 0 10 10"
                  refX="18"
                  refY="5"
                  markerWidth="6"
                  markerHeight="6"
                  orient="auto-start-reverse"
                >
                  <path d="M 0 0 L 10 5 L 0 10 z" fill="#178582" opacity="0.6" />
                </marker>
              </defs>

              <g
                transform={`translate(${svgOffset.x}, ${svgOffset.y}) scale(${zoomLevel})`}
                transform-origin="400 260"
              >
                {/* Edge Lines */}
                {visibleRelationships.map((rel) => {
                  const sourcePos = nodePositions.get(rel.sourceNodeId);
                  const targetPos = nodePositions.get(rel.targetNodeId);
                  if (!sourcePos || !targetPos) return null;

                  const isSelected =
                    selectedNode &&
                    (selectedNode.id === rel.sourceNodeId || selectedNode.id === rel.targetNodeId);

                  return (
                    <g key={rel.id || `${rel.sourceNodeId}-${rel.targetNodeId}`}>
                      <line
                        x1={sourcePos.x}
                        y1={sourcePos.y}
                        x2={targetPos.x}
                        y2={targetPos.y}
                        stroke={isSelected ? '#178582' : '#334155'}
                        strokeWidth={isSelected ? 2 : 1}
                        strokeDasharray={rel.relationType === 'REFERENCES' ? '4,4' : undefined}
                        opacity={isSelected ? 0.9 : 0.4}
                        markerEnd="url(#arrow)"
                      />
                    </g>
                  );
                })}

                {/* Nodes */}
                {filteredNodes.map((node) => {
                  const pos = nodePositions.get(node.id);
                  if (!pos) return null;
                  const config = getNodeColor(node.nodeType);
                  const isSelected = selectedNode?.id === node.id;

                  return (
                    <g
                      key={node.id}
                      transform={`translate(${pos.x}, ${pos.y})`}
                      onClick={() => setSelectedNode(node)}
                      className="cursor-pointer transition-transform duration-150 hover:scale-110"
                    >
                      <circle
                        r={isSelected ? 22 : 16}
                        fill={config.fill}
                        stroke={isSelected ? '#ffffff' : config.stroke}
                        strokeWidth={isSelected ? 3 : 1.5}
                        className="filter drop-shadow-md"
                      />
                      <text
                        textAnchor="middle"
                        dy=".3em"
                        fill={config.text}
                        fontSize={isSelected ? 11 : 9}
                        fontWeight="bold"
                        pointerEvents="none"
                      >
                        {(node.label || node.name || 'NOD').slice(0, 3).toUpperCase()}
                      </text>
                      <text
                        textAnchor="middle"
                        y={isSelected ? 34 : 28}
                        fill="#e2e8f0"
                        fontSize={10}
                        fontWeight={isSelected ? '600' : 'normal'}
                        pointerEvents="none"
                        className="select-none"
                      >
                        {(() => {
                          const lbl = node.label || node.name || 'Node';
                          return lbl.length > 16 ? `${lbl.slice(0, 15)}…` : lbl;
                        })()}
                      </text>
                    </g>
                  );
                })}
              </g>
            </svg>
          )}

          {/* Canvas Floating Legend */}
          <div className="absolute bottom-3 left-3 bg-bg-surface/90 backdrop-blur border border-border-default/80 rounded-xl p-2.5 text-[10px] space-y-1 shadow-md">
            <span className="font-bold text-text-primary block mb-1">Entity Color Key</span>
            <div className="flex items-center gap-1.5 text-text-secondary">
              <span className="w-2.5 h-2.5 rounded-full bg-[#178582]" /> Skill
            </div>
            <div className="flex items-center gap-1.5 text-text-secondary">
              <span className="w-2.5 h-2.5 rounded-full bg-[#bfa181]" /> DSA Topic
            </div>
            <div className="flex items-center gap-1.5 text-text-secondary">
              <span className="w-2.5 h-2.5 rounded-full bg-[#3b82f6]" /> Career Goal
            </div>
            <div className="flex items-center gap-1.5 text-text-secondary">
              <span className="w-2.5 h-2.5 rounded-full bg-[#8b5cf6]" /> Document
            </div>
            <div className="flex items-center gap-1.5 text-text-secondary">
              <span className="w-2.5 h-2.5 rounded-full bg-[#10b981]" /> Learning Task
            </div>
          </div>
        </div>

        {/* Selected Entity Details Drawer */}
        <Card className="h-full">
          <CardHeader>
            <CardTitle className="text-sm">Entity Inspection</CardTitle>
          </CardHeader>
          <CardContent>
            {selectedNode ? (
              <div className="space-y-4">
                <div>
                  <span className="text-[10px] font-bold px-2 py-0.5 rounded border uppercase tracking-wider bg-primary-500/10 text-primary-400 border-primary-500/20">
                    {selectedNode.nodeType}
                  </span>
                  <h3 className="text-base font-bold text-text-primary mt-2">
                    {selectedNode.label || selectedNode.name || 'Selected Node'}
                  </h3>
                  <p className="text-xs text-text-muted mt-0.5 font-mono">
                    ID: {selectedNode.externalId || selectedNode.id}
                  </p>
                </div>

                {/* Node Metadata / Properties */}
                {selectedNode.properties && (
                  <div className="bg-bg-surface border border-border-default rounded-xl p-3 text-xs space-y-1.5">
                    <span className="font-semibold text-text-primary block text-[11px] uppercase tracking-wider">
                      Properties
                    </span>
                    {typeof selectedNode.properties === 'object' ? (
                      Object.entries(selectedNode.properties).map(([k, v]) => (
                        <div key={k} className="flex justify-between text-text-secondary">
                          <span className="text-text-muted">{k}:</span>
                          <span className="font-mono">{String(v)}</span>
                        </div>
                      ))
                    ) : (
                      <p className="text-text-secondary">{String(selectedNode.properties)}</p>
                    )}
                  </div>
                )}

                {/* Incoming Connections */}
                <div>
                  <h4 className="text-xs font-semibold text-text-primary mb-2 flex items-center justify-between">
                    <span>Inbound Influences</span>
                    <span className="text-[10px] font-mono text-text-muted">
                      ({selectedNodeConnections.incoming.length})
                    </span>
                  </h4>
                  {selectedNodeConnections.incoming.length === 0 ? (
                    <p className="text-[11px] text-text-muted italic">No inbound links</p>
                  ) : (
                    <div className="space-y-1.5">
                      {selectedNodeConnections.incoming.map(({ rel, node }) => (
                        <div
                          key={rel.id}
                          onClick={() => setSelectedNode(node)}
                          className="p-2 rounded-lg bg-bg-surface border border-border-default text-xs cursor-pointer hover:border-primary-500/40 transition-colors"
                        >
                          <div className="flex items-center justify-between text-[10px] text-primary-400 font-mono mb-0.5">
                            <span>{rel.relationType}</span>
                            <span>←</span>
                          </div>
                          <span className="font-medium text-text-secondary block">
                            {node.label || node.name || 'Connected Node'}
                          </span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                {/* Outgoing Connections */}
                <div>
                  <h4 className="text-xs font-semibold text-text-primary mb-2 flex items-center justify-between">
                    <span>Outbound Relations</span>
                    <span className="text-[10px] font-mono text-text-muted">
                      ({selectedNodeConnections.outgoing.length})
                    </span>
                  </h4>
                  {selectedNodeConnections.outgoing.length === 0 ? (
                    <p className="text-[11px] text-text-muted italic">No outbound links</p>
                  ) : (
                    <div className="space-y-1.5">
                      {selectedNodeConnections.outgoing.map(({ rel, node }) => (
                        <div
                          key={rel.id}
                          onClick={() => setSelectedNode(node)}
                          className="p-2 rounded-lg bg-bg-surface border border-border-default text-xs cursor-pointer hover:border-primary-500/40 transition-colors"
                        >
                          <div className="flex items-center justify-between text-[10px] text-primary-400 font-mono mb-0.5">
                            <span>{rel.relationType}</span>
                            <span>→</span>
                          </div>
                          <span className="font-medium text-text-secondary block">
                            {node.label || node.name || 'Connected Node'}
                          </span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            ) : (
              <div className="text-center py-8 text-text-muted">
                <span className="text-2xl block mb-2">👆</span>
                <p className="text-xs">Click on any node in the graph to inspect its properties and connected academic entities.</p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
