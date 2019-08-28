import React from 'react';
import './App.css';

import JokeComponent from './components/JokeComponent';

function App() {
  return (
    <div className="App">
      <h2 className="App__title">Welcome to the <span className="App__logo">Laugh Factory</span></h2>
      {<JokeComponent />}
    </div>
  );
}

export default App;
