import { Box } from "@mui/material";
import { useState, type PropsWithChildren } from "react";
import { BrowserRouter, Route, Routes } from "react-router-dom";
import { ToastContainer } from "react-toastify";
import { Sidebar } from "./components/Sidebar";
import { TopBar } from "./components/TopBar";
import { DashboardPage } from "./pages/DashboardPage";
import FilesPage from "./pages/FilesPage";
import { HealthPage } from "./pages/HealthPage";
import { LogsPage } from "./pages/LogsPage";
import { SettingsPage } from "./pages/SettingsPage";
import { SseProvider } from "./sse/SseProvider";
import { EncryptionStatusProvider } from "./status/EncryptionStatusProvider";
import { JottaDaemonStatusProvider } from "./status/JottaDaemonStatusProvider";
import { JottaStatusProvider } from "./status/JottaStatusProvider";

interface AppLayoutProps {
  children: React.ReactNode
}

export function AppLayout({ children }: AppLayoutProps) {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const drawerWidth = sidebarOpen ? 260 : 64

  return (
    <Box
      sx={{
        display: "flex",
        width: "100vw",     // ← kritisk
        height: "100vh",    // ← kritisk
        overflow: "hidden"  // ← hindrer scroll her
      }}
    >
      <TopBar onToggleSidebar={() => setSidebarOpen(prev => !prev)} />

      <Sidebar open={sidebarOpen} />

      <Box
        component="main"
        sx={{
          flexGrow: 1,
          ml: `${drawerWidth}px`,
          mt: "64px",
          width: `calc(100vw - ${drawerWidth}px)`, // ← kritisk
          height: `calc(100vh - 56px)`,            // ← kritisk
          overflow: "hidden",                      // ← main skal ikke scrolle
          position: "relative"                     // ← for sticky i child
        }}
      >
        {children}
      </Box>
    </Box>
  )
}

export function AppProviders({ children }: PropsWithChildren) {
  return (
    <SseProvider>
      <JottaStatusProvider>
        <JottaDaemonStatusProvider>
          <EncryptionStatusProvider>
            {children}
          </EncryptionStatusProvider>
        </JottaDaemonStatusProvider>
      </JottaStatusProvider>
    </SseProvider>
  )
}


export default function App() {

  return (
    <BrowserRouter>
      <AppProviders>
        <AppLayout>
          <Routes>
            <Route path="/" element={<DashboardPage />} />
            <Route path="/settings" element={<SettingsPage />} />
            <Route path="/health" element={<HealthPage />} />
            <Route path="/files" element={<FilesPage />} />
            <Route path="/logs" element={<LogsPage />} />
          </Routes>
          <ToastContainer
            position='bottom-left'
            autoClose={3000}
            hideProgressBar={true}
            newestOnTop={true}
            closeOnClick
            pauseOnHover
            theme='dark'
          />
        </AppLayout>
      </AppProviders>
    </BrowserRouter>
  );
}
