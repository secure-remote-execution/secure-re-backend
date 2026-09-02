import { BrowserRouter, Navigate, NavLink, Route, Routes } from "react-router-dom";
import { DeviceListPage } from "./pages/DeviceListPage";
import { DeviceDetailPage } from "./pages/DeviceDetailPage";
import { ScriptSimulatorPage } from "./pages/ScriptSimulatorPage";

function App() {
  return (
    <BrowserRouter>
      <div className="app-shell">
        <header className="app-header">
          <h1>Secure Product Challenge</h1>
          <p className="app-subtitle">
            Laboratorio 3 - FDSI (ECI) · Inventario ficticio de red y simulación de scripts
          </p>
          <nav className="app-nav">
            <NavLink to="/devices">Dispositivos</NavLink>
            <NavLink to="/simulate">Simular script</NavLink>
          </nav>
        </header>

        <main className="app-content">
          <Routes>
            <Route path="/" element={<Navigate to="/devices" replace />} />
            <Route path="/devices" element={<DeviceListPage />} />
            <Route path="/devices/:id" element={<DeviceDetailPage />} />
            <Route path="/simulate" element={<ScriptSimulatorPage />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}

export default App;
