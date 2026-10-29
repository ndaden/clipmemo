import React, { useState } from 'react';
import {
  X,
  Languages,
  Key,
  Smartphone,
  Monitor,
  Download,
  Upload,
  RotateCcw,
  Check,
  ShieldCheck,
  HelpCircle,
} from 'lucide-react';
import { AppSettings } from '../types/note';
import { StorageService } from '../services/storage';

interface SettingsDialogProps {
  isOpen: boolean;
  onClose: () => void;
  settings: AppSettings;
  onUpdateSettings: (newSettings: AppSettings) => void;
  onResetNotes: () => void;
  onOpenOnboarding: () => void;
}

export const SettingsDialog: React.FC<SettingsDialogProps> = ({
  isOpen,
  onClose,
  settings,
  onUpdateSettings,
  onResetNotes,
  onOpenOnboarding,
}) => {
  if (!isOpen) return null;
  const isFr = settings.language === 'fr';

  const [apiKeyInput, setApiKeyInput] = useState(settings.geminiApiKey || '');
  const [apiKeySaved, setApiKeySaved] = useState(false);
  const [importStatus, setImportStatus] = useState<string | null>(null);

  const handleSaveApiKey = () => {
    onUpdateSettings({ ...settings, geminiApiKey: apiKeyInput.trim() });
    setApiKeySaved(true);
    setTimeout(() => setApiKeySaved(false), 2000);
  };

  const handleLanguageChange = (lang: 'fr' | 'en') => {
    onUpdateSettings({ ...settings, language: lang });
  };

  const handleViewModeChange = (mode: 'responsive' | 'mobile-mockup') => {
    onUpdateSettings({ ...settings, viewMode: mode });
  };

  const handleExportBackup = () => {
    const jsonStr = StorageService.exportBackupJson();
    const blob = new Blob([jsonStr], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `reelnotes-backup-${new Date().toISOString().slice(0, 10)}.json`;
    a.click();
    URL.revokeObjectURL(url);
  };

  const handleImportFile = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = event => {
      const content = event.target?.result as string;
      if (content) {
        const success = StorageService.importBackupJson(content);
        if (success) {
          setImportStatus(isFr ? 'Importation réussie !' : 'Import successful!');
          setTimeout(() => {
            window.location.reload();
          }, 1000);
        } else {
          setImportStatus(isFr ? 'Fichier de sauvegarde invalide.' : 'Invalid backup file.');
        }
      }
    };
    reader.readAsText(file);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-fadeIn">
      <div className="relative w-full max-w-lg bg-[#0E1017] rounded-3xl border border-[#232733] shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-[#232733] bg-[#14161D]">
          <h2 className="font-bold text-base text-white">
            {isFr ? 'Paramètres' : 'Settings'}
          </h2>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-[#1C1F2B] transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Scrollable content */}
        <div className="p-6 overflow-y-auto space-y-6 flex-1 text-xs">
          {/* Section 1: Language */}
          <div>
            <div className="flex items-center gap-2 mb-2 font-bold text-slate-200">
              <Languages className="w-4 h-4 text-violet-400" />
              <span>{isFr ? 'Langue de l’application' : 'Application Language'}</span>
            </div>
            <div className="grid grid-cols-2 gap-2">
              <button
                onClick={() => handleLanguageChange('fr')}
                className={`py-2 px-3 rounded-xl border font-semibold flex items-center justify-center gap-2 transition-all cursor-pointer ${
                  settings.language === 'fr'
                    ? 'bg-violet-600/20 border-violet-500 text-violet-300'
                    : 'bg-[#14161D] border-[#232733] text-slate-400 hover:text-white'
                }`}
              >
                <span>🇫🇷 Français</span>
                {settings.language === 'fr' && <Check className="w-3.5 h-3.5 text-violet-400" />}
              </button>
              <button
                onClick={() => handleLanguageChange('en')}
                className={`py-2 px-3 rounded-xl border font-semibold flex items-center justify-center gap-2 transition-all cursor-pointer ${
                  settings.language === 'en'
                    ? 'bg-violet-600/20 border-violet-500 text-violet-300'
                    : 'bg-[#14161D] border-[#232733] text-slate-400 hover:text-white'
                }`}
              >
                <span>🇬🇧 English</span>
                {settings.language === 'en' && <Check className="w-3.5 h-3.5 text-violet-400" />}
              </button>
            </div>
          </div>

          {/* Section 2: View Mode (Responsive vs Mobile Mockup) */}
          <div>
            <div className="flex items-center gap-2 mb-2 font-bold text-slate-200">
              <Smartphone className="w-4 h-4 text-cyan-400" />
              <span>{isFr ? 'Mode d’affichage' : 'Display Layout'}</span>
            </div>
            <div className="grid grid-cols-2 gap-2">
              <button
                onClick={() => handleViewModeChange('responsive')}
                className={`py-2.5 px-3 rounded-xl border font-semibold flex items-center justify-center gap-2 transition-all cursor-pointer ${
                  settings.viewMode === 'responsive'
                    ? 'bg-cyan-600/20 border-cyan-500 text-cyan-300'
                    : 'bg-[#14161D] border-[#232733] text-slate-400 hover:text-white'
                }`}
              >
                <Monitor className="w-3.5 h-3.5" />
                <span>{isFr ? 'Plein écran fluide' : 'Full Responsive'}</span>
              </button>
              <button
                onClick={() => handleViewModeChange('mobile-mockup')}
                className={`py-2.5 px-3 rounded-xl border font-semibold flex items-center justify-center gap-2 transition-all cursor-pointer ${
                  settings.viewMode === 'mobile-mockup'
                    ? 'bg-cyan-600/20 border-cyan-500 text-cyan-300'
                    : 'bg-[#14161D] border-[#232733] text-slate-400 hover:text-white'
                }`}
              >
                <Smartphone className="w-3.5 h-3.5" />
                <span>{isFr ? 'Cadre mobile Pixel' : 'Pixel Mobile Frame'}</span>
              </button>
            </div>
          </div>

          {/* Section 3: Gemini API Key */}
          <div>
            <div className="flex items-center justify-between mb-1.5">
              <div className="flex items-center gap-2 font-bold text-slate-200">
                <Key className="w-4 h-4 text-amber-400" />
                <span>{isFr ? 'Clé API Gemini (Optionnel)' : 'Gemini API Key (Optional)'}</span>
              </div>
              <span className="text-[10px] text-emerald-400 flex items-center gap-1 font-semibold">
                <ShieldCheck className="w-3 h-3" />
                {isFr ? 'Fallback local actif' : 'Offline fallback active'}
              </span>
            </div>
            <p className="text-[11px] text-slate-400 mb-2 leading-relaxed">
              {isFr
                ? 'Si aucune clé n’est renseignée, le moteur heuristique local intégré extrait automatiquement les ingrédients, étapes et métriques.'
                : 'If omitted, the built-in offline heuristic extractor parses ingredients, steps, and timings automatically.'}
            </p>
            <div className="flex gap-2">
              <input
                type="password"
                value={apiKeyInput}
                onChange={e => setApiKeyInput(e.target.value)}
                placeholder="AIzaSy..."
                className="flex-1 bg-[#14161D] border border-[#232733] focus:border-violet-500 rounded-xl px-3 py-2 text-white placeholder-slate-600 outline-none transition-colors"
              />
              <button
                onClick={handleSaveApiKey}
                className="px-3.5 py-2 rounded-xl bg-violet-600 hover:bg-violet-500 text-white font-bold transition-all cursor-pointer"
              >
                {apiKeySaved ? (isFr ? 'Enregistré !' : 'Saved!') : (isFr ? 'Enregistrer' : 'Save')}
              </button>
            </div>
          </div>

          {/* Section 4: Data Management */}
          <div>
            <div className="font-bold text-slate-200 mb-2">
              {isFr ? 'Gestion des données & Sauvegarde' : 'Data & Backup'}
            </div>
            <div className="space-y-2">
              <button
                onClick={handleExportBackup}
                className="w-full py-2.5 px-3 rounded-xl bg-[#14161D] hover:bg-[#1C1F2B] border border-[#232733] text-slate-300 font-semibold flex items-center justify-between transition-colors cursor-pointer"
              >
                <div className="flex items-center gap-2">
                  <Download className="w-4 h-4 text-violet-400" />
                  <span>{isFr ? 'Exporter les notes en JSON' : 'Export notes to JSON'}</span>
                </div>
              </button>

              <label className="w-full py-2.5 px-3 rounded-xl bg-[#14161D] hover:bg-[#1C1F2B] border border-[#232733] text-slate-300 font-semibold flex items-center justify-between transition-colors cursor-pointer">
                <div className="flex items-center gap-2">
                  <Upload className="w-4 h-4 text-cyan-400" />
                  <span>{isFr ? 'Importer une sauvegarde JSON' : 'Import JSON backup'}</span>
                </div>
                <input
                  type="file"
                  accept=".json"
                  onChange={handleImportFile}
                  className="hidden"
                />
              </label>

              {importStatus && (
                <div className="p-2 rounded-lg bg-emerald-950/40 border border-emerald-500/30 text-emerald-300 text-[11px]">
                  {importStatus}
                </div>
              )}

              <button
                onClick={() => {
                  if (
                    confirm(
                      isFr
                        ? 'Réinitialiser toutes vos notes avec les exemples par défaut ?'
                        : 'Reset all notes with the initial default samples?'
                    )
                  ) {
                    onResetNotes();
                  }
                }}
                className="w-full py-2.5 px-3 rounded-xl bg-[#14161D] hover:bg-red-950/20 border border-[#232733] hover:border-red-500/30 text-slate-400 hover:text-red-300 font-semibold flex items-center justify-between transition-colors cursor-pointer"
              >
                <div className="flex items-center gap-2">
                  <RotateCcw className="w-4 h-4 text-red-400" />
                  <span>{isFr ? 'Réinitialiser avec les notes d’exemple' : 'Reset to sample notes'}</span>
                </div>
              </button>
            </div>
          </div>

          {/* Section 5: Re-show Onboarding */}
          <div className="pt-2 border-t border-[#232733] flex items-center justify-between">
            <button
              onClick={() => {
                onClose();
                onOpenOnboarding();
              }}
              className="text-violet-400 hover:text-violet-300 font-semibold flex items-center gap-1.5 cursor-pointer"
            >
              <HelpCircle className="w-4 h-4" />
              <span>{isFr ? 'Revoir le guide de bienvenue' : 'Review welcome guide'}</span>
            </button>
            <span className="text-[10px] text-slate-600 font-mono">v1.2.1 • React</span>
          </div>
        </div>
      </div>
    </div>
  );
};
