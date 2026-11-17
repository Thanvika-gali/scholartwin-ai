export const DSA_TOPICS = [
  'Arrays',
  'Strings',
  'Searching',
  'Sorting',
  'Hashing',
  'Two Pointers',
  'Sliding Window',
  'Linked Lists',
  'Stacks',
  'Queues',
  'Binary Trees',
  'Binary Search Trees',
  'Heaps',
  'Graphs',
  'Greedy Algorithms',
  'Backtracking',
  'Dynamic Programming',
  'Recursion',
  'Bit Manipulation',
  'Tries',
];

export const DSA_PLATFORMS = [
  'LeetCode',
  'GeeksforGeeks',
  'HackerRank',
  'Codeforces',
  'CodeChef',
  'AtCoder',
  'Other',
];

export const DSA_DIFFICULTIES = [
  { value: 'EASY', label: 'Easy', color: 'emerald' },
  { value: 'MEDIUM', label: 'Medium', color: 'amber' },
  { value: 'HARD', label: 'Hard', color: 'rose' },
];

export const DSA_STATUSES = [
  { value: 'NOT_STARTED', label: 'Not Started', color: 'slate' },
  { value: 'ATTEMPTED', label: 'Attempted', color: 'amber' },
  { value: 'SOLVED', label: 'Solved', color: 'teal' },
];

export const PROGRAMMING_LANGUAGES = [
  'Java',
  'Python',
  'C++',
  'JavaScript',
  'TypeScript',
  'C',
  'Go',
  'Rust',
  'Kotlin',
  'Other',
];

/**
 * Autodetect platform name from URL
 */
export function detectPlatformFromUrl(url) {
  if (!url) return 'Other';
  const lower = url.toLowerCase();
  if (lower.includes('leetcode.com')) return 'LeetCode';
  if (lower.includes('geeksforgeeks.org')) return 'GeeksforGeeks';
  if (lower.includes('hackerrank.com')) return 'HackerRank';
  if (lower.includes('codeforces.com')) return 'Codeforces';
  if (lower.includes('codechef.com')) return 'CodeChef';
  if (lower.includes('atcoder.jp')) return 'AtCoder';
  return 'Other';
}

