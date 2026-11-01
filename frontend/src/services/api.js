/**
 * API Service for AI ScholarTwin
 * Communicates with backend endpoints through the Vite proxy (/api -> :8080)
 */

export async function fetchHealthStatus() {
  try {
    const response = await fetch('/api/health', {
      headers: {
        'Accept': 'application/json',
      },
    });

    if (!response.ok) {
      throw new Error(`Backend responded with HTTP status ${response.status}`);
    }

    const data = await response.json();
    return {
      success: true,
      data,
    };
  } catch (error) {
    return {
      success: false,
      error: error.message || 'Unable to connect to backend',
    };
  }
}
