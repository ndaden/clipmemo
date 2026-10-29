import React, { useState, useEffect, useMemo } from 'react';
import {
  Search,
  Plus,
  Star,
  SlidersHorizontal,
  Settings,
  HelpCircle,
  X,
  Sparkles,
  Smartphone,
  Monitor,
  Flame,
  Film,
} from 'lucide-react';
import { AppLogo } from './components/AppLogo';
import { CategoryChipRow } from './components/CategoryChipRow';
import { NoteCard } from './components/NoteCard';
import { NoteDetailModal } from './components/NoteDetailModal';
import { AddReelDialog } from './components/AddReelDialog';
import { SettingsDialog } from './components/SettingsDialog';
import { OnboardingDialog } from './components/OnboardingDialog';
import { ProcessingOverlay } from './components/ProcessingOverlay';
import { StorageService } from './services/storage';
import { ReelNote, NoteCategory, AppSettings } from './types/note';

export const App: React.FC = () => {
  const [notes, setNotes] = useState<ReelNote[]>(() => StorageService.getNotes());
  const [settings, setSettings] = useState<AppSettings>(() => StorageService.getSettings());
  const [selectedCategory, setSelectedCategory] = useState<NoteCategory | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [onlyFavorites, setOnlyFavorites] = useState(false);
  const [selectedNote, setSelectedNote] = useState<ReelNote | null>(null);
  const [isAddOpen, setIsAddOpen] = useState(false);
  const [isSettingsOpen, setIsSettingsOpen] = useState(false);
  const [isOnboardingOpen, setIsOnboardingOpen] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);
  const [processingStatus, setProcessingStatus] = useState('');

  const isFr = settings.language === 'fr';

  // Check onboarding on initial mount
  useEffect(() => {
    if (!settings.hasSeenOnboarding) {
      setIsOnboardingOpen(true);
    }
  }, [settings.hasSeenOnboarding]);

  const handleDismissOnboarding = () => {
    setIsOnboardingOpen(false);
    const updated = { ...settings, hasSeenOnboarding: true };
    setSettings(updated);
    StorageService.saveSettings(updated);
  };

  const handleUpdateSettings = (newSettings: AppSettings) => {
    setSettings(newSettings);
    StorageService.saveSettings(newSettings);
  };

  const handleResetNotes = () => {
    const defaultNotes = StorageService.resetToDefaultNotes();
    setNotes(defaultNotes);
    setSelectedNote(null);
  };

  const handleToggleFavorite = (e: React.MouseEvent | null, noteId: string) => {
    e?.stopPropagation();
    const updated = StorageService.toggleFavorite(noteId);
    setNotes(updated);
    if (selectedNote && selectedNote.id === noteId) {
      const match = updated.find(n => n.id === noteId);
      if (match) setSelectedNote(match);
    }
  };

  const handleDeleteNote = (e: React.MouseEvent | null, noteId: string) => {
    e?.stopPropagation();
    const updated = StorageService.deleteNote(noteId);
    setNotes(updated);
    if (selectedNote && selectedNote.id === noteId) {
      setSelectedNote(null);
    }
  };

  const handleToggleIngredient = (note: ReelNote, ingredientIndex: number) => {
    const updatedIngredients = [...note.structuredData.ingredients];
    if (updatedIngredients[ingredientIndex]) {
      updatedIngredients[ingredientIndex] = {
        ...updatedIngredients[ingredientIndex],
        isChecked: !updatedIngredients[ingredientIndex].isChecked,
      };
      const updatedNote: ReelNote = {
        ...note,
        structuredData: {
          ...note.structuredData,
          ingredients: updatedIngredients,
        },
      };
      const newNotes = StorageService.updateNote(updatedNote);
      setNotes(newNotes);
      setSelectedNote(updatedNote);
    }
  };

  const handleToggleStep = (note: ReelNote, stepNumber: number) => {
    const updatedSteps = note.structuredData.steps.map(s => {
      if (s.stepNumber === stepNumber) {
        return { ...s, isDone: !s.isDone };
      }
      return s;
    });

    const updatedNote: ReelNote = {
      ...note,
      structuredData: {
        ...note.structuredData,
        steps: updatedSteps,
      },
    };
    const newNotes = StorageService.updateNote(updatedNote);
    setNotes(newNotes);
    setSelectedNote(updatedNote);
  };

  const handleNoteCreated = (newNote: ReelNote) => {
    const updated = StorageService.addNote(newNote);
    setNotes(updated);
    setSelectedNote(newNote);
  };

  // Category counts computation
  const categoryCounts = useMemo(() => {
    const counts: Record<string, number> = {};
    for (const note of notes) {
      counts[note.category] = (counts[note.category] || 0) + 1;
    }
    return counts;
  }, [notes]);

  // Filtered notes
  const filteredNotes = useMemo(() => {
    return notes.filter(note => {
      const matchesCategory = selectedCategory === null || note.category === selectedCategory;
      const matchesFavorites = !onlyFavorites || note.isFavorite;

      const q = searchQuery.trim().toLowerCase();
      const matchesSearch =
        !q ||
        note.title.toLowerCase().includes(q) ||
        note.summary.toLowerCase().includes(q) ||
        note.author?.toLowerCase().includes(q) ||
        note.tags.some(t => t.toLowerCase().includes(q)) ||
        note.rawCaption.toLowerCase().includes(q);

      return matchesCategory && matchesFavorites && matchesSearch;
    });
  }, [notes, selectedCategory, onlyFavorites, searchQuery]);

  // Main Feed Content (shared between responsive and phone mockup)
  const MainContent = (
    <div className="flex-1 flex flex-col min-h-0">
      {/* Search and Quick Filters Bar */}
      <div className="px-4 py-3 bg-[#11131A]/70 backdrop-blur-md border-b border-[#232733] shrink-0 space-y-2.5">
        <div className="flex items-center gap-2">
          {/* Search box */}
          <div className="relative flex-1">
            <Search className="w-4 h-4 text-slate-500 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              placeholder={
                isFr
                  ? 'Rechercher une recette, créateur, tag, ingrédient...'
                  : 'Search notes, ingredients, creators, tags...'
              }
              className="w-full bg-[#181B24] border border-[#232733] focus:border-violet-500 rounded-full pl-9 pr-8 py-2 text-xs text-white placeholder-slate-500 outline-none transition-colors"
            />
            {searchQuery && (
              <button
                onClick={() => setSearchQuery('')}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500 hover:text-white"
              >
                <X className="w-3.5 h-3.5" />
              </button>
            )}
          </div>

          {/* Favorites filter toggle */}
          <button
            onClick={() => setOnlyFavorites(!onlyFavorites)}
            className={`flex items-center gap-1.5 px-3 py-2 rounded-full text-xs font-semibold border transition-all cursor-pointer shrink-0 ${
              onlyFavorites
                ? 'bg-amber-500/20 border-amber-500/50 text-amber-300'
                : 'bg-[#181B24] border-[#232733] text-slate-400 hover:text-white'
            }`}
          >
            <Star className={`w-3.5 h-3.5 ${onlyFavorites ? 'fill-amber-400' : ''}`} />
            <span className="hidden sm:inline">{isFr ? 'Favoris' : 'Favorites'}</span>
          </button>
        </div>

        {/* Category horizontal scroll */}
        <CategoryChipRow
          selectedCategory={selectedCategory}
          onSelectCategory={setSelectedCategory}
          categoryCounts={categoryCounts}
          totalCount={notes.length}
          language={settings.language}
        />
      </div>

      {/* Notes Grid */}
      <div className="flex-1 overflow-y-auto p-4 sm:p-6">
        {filteredNotes.length === 0 ? (
          <div className="h-64 flex flex-col items-center justify-center text-center p-6 bg-[#14161D]/50 rounded-3xl border border-dashed border-[#232733] my-6">
            <div className="w-12 h-12 rounded-2xl bg-violet-600/10 border border-violet-500/20 flex items-center justify-center text-violet-400 mb-3">
              <Film className="w-6 h-6" />
            </div>
            <h3 className="font-bold text-sm text-slate-200 mb-1">
              {isFr ? 'Aucune note trouvée' : 'No notes found'}
            </h3>
            <p className="text-xs text-slate-400 max-w-xs mb-4">
              {searchQuery || onlyFavorites || selectedCategory
                ? (isFr
                    ? 'Essayez de modifier vos filtres ou votre recherche.'
                    : 'Try clearing your search or category filters.')
                : (isFr
                    ? 'Ajoutez votre premier Reel Instagram pour le transformer en note !'
                    : 'Add your first Instagram Reel to transform it into a note!')}
            </p>
            <button
              onClick={() => setIsAddOpen(true)}
              className="flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-bold bg-violet-600 hover:bg-violet-500 text-white shadow-lg shadow-violet-500/25 transition-all cursor-pointer"
            >
              <Plus className="w-4 h-4" />
              <span>{isFr ? 'Ajouter un Reel' : 'Add a Reel'}</span>
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            {filteredNotes.map(note => (
              <NoteCard
                key={note.id}
                note={note}
                onSelect={setSelectedNote}
                onToggleFavorite={handleToggleFavorite}
                onDelete={handleDeleteNote}
                language={settings.language}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );

  return (
    <div className="min-h-screen bg-[#0A0B0E] text-[#F8FAFC] flex flex-col">
      {/* Top Navigation Bar */}
      <header className="sticky top-0 z-40 bg-[#14161D]/80 backdrop-blur-xl border-b border-[#232733] px-4 sm:px-8 py-3.5 flex items-center justify-between">
        <AppLogo size="md" />

        {/* Header Right Actions */}
        <div className="flex items-center gap-2">
          {/* Layout switcher */}
          <button
            onClick={() =>
              handleUpdateSettings({
                ...settings,
                viewMode: settings.viewMode === 'responsive' ? 'mobile-mockup' : 'responsive',
              })
            }
            className="p-2 rounded-xl bg-[#1C1F2B] hover:bg-[#252A3B] border border-[#282C3D] text-slate-300 hover:text-white transition-colors cursor-pointer"
            title={
              settings.viewMode === 'responsive'
                ? (isFr ? 'Basculer en cadre mobile Pixel' : 'Switch to Pixel mobile mockup')
                : (isFr ? 'Basculer en plein écran fluide' : 'Switch to full responsive')
            }
          >
            {settings.viewMode === 'responsive' ? (
              <Smartphone className="w-4 h-4 text-cyan-400" />
            ) : (
              <Monitor className="w-4 h-4 text-cyan-400" />
            )}
          </button>

          {/* Quick Language Toggle */}
          <button
            onClick={() =>
              handleUpdateSettings({
                ...settings,
                language: settings.language === 'fr' ? 'en' : 'fr',
              })
            }
            className="px-2.5 py-1.5 rounded-xl bg-[#1C1F2B] hover:bg-[#252A3B] border border-[#282C3D] text-xs font-bold text-slate-300 hover:text-white transition-colors cursor-pointer"
            title={isFr ? 'Changer de langue' : 'Change language'}
          >
            {settings.language === 'fr' ? '🇫🇷 FR' : '🇬🇧 EN'}
          </button>

          {/* Settings button */}
          <button
            onClick={() => setIsSettingsOpen(true)}
            className="p-2 rounded-xl bg-[#1C1F2B] hover:bg-[#252A3B] border border-[#282C3D] text-slate-300 hover:text-white transition-colors cursor-pointer"
            title={isFr ? 'Paramètres' : 'Settings'}
          >
            <Settings className="w-4 h-4 text-slate-300" />
          </button>

          {/* New Reel Primary CTA */}
          <button
            onClick={() => setIsAddOpen(true)}
            className="flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-extrabold bg-gradient-to-r from-violet-600 via-purple-600 to-cyan-500 hover:from-violet-500 hover:to-cyan-400 text-white shadow-lg shadow-violet-500/30 hover:scale-[1.02] active:scale-95 transition-all cursor-pointer"
          >
            <Plus className="w-4 h-4 stroke-[3]" />
            <span className="hidden sm:inline">{isFr ? 'Nouveau Reel' : 'New Reel'}</span>
          </button>
        </div>
      </header>

      {/* Main Body: Mode Check */}
      {settings.viewMode === 'responsive' ? (
        // Standard Responsive Desktop / Tablet / Mobile layout
        <main className="flex-1 flex flex-col">{MainContent}</main>
      ) : (
        // Pixel 10 Pro Mobile Phone Frame Preview
        <main className="flex-1 flex flex-col items-center justify-center p-4 sm:p-8 bg-gradient-to-b from-[#060709] to-[#0A0B0E]">
          <div className="relative w-full max-w-[420px] h-[850px] max-h-[92vh] bg-[#0A0B0E] rounded-[48px] border-[7px] border-[#232733] shadow-[0_0_0_2px_rgba(255,255,255,0.08),0_25px_60px_-15px_rgba(0,0,0,0.9),0_0_80px_rgba(139,92,246,0.15)] flex flex-col overflow-hidden">
            {/* Phone Status Bar */}
            <div className="h-11 px-6 flex items-center justify-between text-[11px] font-bold text-slate-300 z-30 shrink-0 select-none">
              <span>09:41</span>
              {/* Punch Hole Camera */}
              <div className="w-3.5 h-3.5 bg-black rounded-full border border-[#1C1F2B]" />
              <div className="flex items-center gap-1.5">
                <span>5G</span>
                <span>100%</span>
              </div>
            </div>

            {/* Mobile View Container */}
            <div className="flex-1 flex flex-col overflow-hidden relative">
              {MainContent}
            </div>

            {/* Bottom floating Add button in mobile frame */}
            <div className="absolute bottom-5 right-5 z-20">
              <button
                onClick={() => setIsAddOpen(true)}
                className="w-12 h-12 rounded-full bg-gradient-to-tr from-violet-600 to-cyan-500 flex items-center justify-center text-white shadow-xl shadow-violet-600/40 hover:scale-105 active:scale-95 transition-all cursor-pointer"
              >
                <Plus className="w-6 h-6 stroke-[3]" />
              </button>
            </div>
          </div>
        </main>
      )}

      {/* Modals and Overlays */}
      {selectedNote && (
        <NoteDetailModal
          note={selectedNote}
          onClose={() => setSelectedNote(null)}
          onToggleFavorite={noteId => handleToggleFavorite(null, noteId)}
          onDelete={noteId => handleDeleteNote(null, noteId)}
          onToggleIngredient={handleToggleIngredient}
          onToggleStep={handleToggleStep}
          language={settings.language}
        />
      )}

      <AddReelDialog
        isOpen={isAddOpen}
        onClose={() => setIsAddOpen(false)}
        onNoteCreated={handleNoteCreated}
        language={settings.language}
        geminiApiKey={settings.geminiApiKey}
        onStartProcessing={status => {
          setIsProcessing(true);
          setProcessingStatus(status);
        }}
        onEndProcessing={() => {
          setIsProcessing(false);
          setProcessingStatus('');
        }}
      />

      <SettingsDialog
        isOpen={isSettingsOpen}
        onClose={() => setIsSettingsOpen(false)}
        settings={settings}
        onUpdateSettings={handleUpdateSettings}
        onResetNotes={handleResetNotes}
        onOpenOnboarding={() => setIsOnboardingOpen(true)}
      />

      <OnboardingDialog
        isOpen={isOnboardingOpen}
        onClose={handleDismissOnboarding}
        language={settings.language}
      />

      <ProcessingOverlay
        isProcessing={isProcessing}
        statusText={processingStatus}
      />
    </div>
  );
};

export default App;
