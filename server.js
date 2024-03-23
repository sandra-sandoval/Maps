import express from 'express';
import { join } from 'path'; // Import the path module
import { fileURLToPath } from 'url';
import { dirname } from 'path';
import cors from 'cors';

//create local server to host files in project directory
const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const app = express(); //express helps set up http server
app.use(cors()); //cors allows us to access resources from remote host 
const port = 3000;

// Serve load JSON file
app.get('/load', (req, res) => {
  const filePath = join(__dirname, '/src/frontend/public/load.json');
  res.sendFile(filePath);
});

// Serve view JSON file
app.get('/view', (req, res) => {
    const filePath = join(__dirname, '/src/frontend/public/view.json');
    res.sendFile(filePath);
  });

// Serve search JSON file
app.get('/search', (req, res) => {
    const filePath = join(__dirname, '/src/frontend/public/search.json');
    res.sendFile(filePath);
  });

// Serve broadband JSON file
app.get('/broadband', (req, res) => {
    const filePath = join(__dirname, '/src/frontend/public/broadband.json');
    res.sendFile(filePath);
  });
  

// Start the server
app.listen(port, () => {
  console.log(`Server is running on http://localhost:${port}`);
});
