const express = require('express');
const path = require('path');
const fs = require('fs');
const { createProxyMiddleware } = require('http-proxy-middleware');

const app = express();
const PORT = process.env.PORT || 3000;
const SPRING_BOOT_URL = 'http://localhost:8081';

// Middleware to parse JSON
app.use(express.json());

// Proxy configuration for admin routes
app.use('/admin', createProxyMiddleware({
  target: SPRING_BOOT_URL,
  changeOrigin: true,
  onError: (err, req, res) => {
    console.error('Proxy error for admin routes:', err.message);
    res.status(503).send('Backend service unavailable');
  }
}));

// Proxy configuration for API routes (except contact form submission)
app.use('/api/admin', createProxyMiddleware({
  target: SPRING_BOOT_URL,
  changeOrigin: true,
  onError: (err, req, res) => {
    console.error('Proxy error for admin API routes:', err.message);
    res.status(503).send('Backend service unavailable');
  }
}));

// Serve static files from the src directory
app.use(express.static(path.join(__dirname, 'src')));

// Default route: serve HOME.html if root is accessed
app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'src', 'HOME.html'));
});

// Contact form submission - Forward to Spring Boot backend only
app.post('/api/contact', async (req, res) => {
  const submission = req.body;

  try {
    // Forward to Spring Boot backend (PostgreSQL database)
    const fetch = (await import('node-fetch')).default;
    const response = await fetch(`${SPRING_BOOT_URL}/api/contact`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(submission)
    });

    if (response.ok) {
      const result = await response.json();
      res.json(result);
    } else {
      throw new Error('Spring Boot backend not available');
    }
  } catch (error) {
    console.error('Spring Boot backend not available:', error.message);
    res.status(500).json({
      success: false,
      message: 'Database service unavailable. Please try again later.'
    });
  }
});

// Endpoint to analyze submissions - Get data from Spring Boot backend
app.get('/api/contact/summary', async (req, res) => {
  try {
    const fetch = (await import('node-fetch')).default;
    const response = await fetch(`${SPRING_BOOT_URL}/api/admin/submissions`);

    if (response.ok) {
      const submissions = await response.json();
      // Analyze the data from database
      const count = submissions.length;
      const serviceStats = {};
      submissions.forEach(s => {
        const service = s.service || 'Unknown';
        serviceStats[service] = (serviceStats[service] || 0) + 1;
      });
      res.json({ count, serviceStats });
    } else {
      throw new Error('Spring Boot backend not available');
    }
  } catch (error) {
    console.error('Error fetching submissions summary:', error.message);
    res.status(500).json({
      count: 0,
      serviceStats: {},
      error: 'Database service unavailable'
    });
  }
});

// Endpoint to get all submissions - Get data from Spring Boot backend
app.get('/api/contact/submissions', async (req, res) => {
  try {
    const fetch = (await import('node-fetch')).default;
    const response = await fetch(`${SPRING_BOOT_URL}/api/admin/submissions`);

    if (response.ok) {
      const submissions = await response.json();
      res.json(submissions);
    } else {
      throw new Error('Spring Boot backend not available');
    }
  } catch (error) {
    console.error('Error fetching submissions:', error.message);
    res.status(500).json({
      error: 'Database service unavailable',
      submissions: []
    });
  }
});

app.listen(PORT, () => {
  console.log(`Server is running at http://localhost:${PORT}`);
});
