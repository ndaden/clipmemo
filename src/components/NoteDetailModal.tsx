import React, { useState } from 'react';
import {
  ArrowLeft,
  Star,
  Copy,
  Check,
  Share2,
  ExternalLink,
  Trash2,
  Clock,
  Flame,
  Users,
  Lightbulb,
  CheckCircle2,
  FileText,
  AlignLeft,
  Sparkles,
} from 'lucide-react';
import { ReelNote, CATEGORIES_CONFIG } from '../types/note';
import { RecipeChecklist } from './RecipeChecklist';
import { StepList } from './StepList';

interface NoteDetailModalProps {
  note: ReelNote;
  onClose: () => void;
  onToggleFavorite: (noteId: string) => void;
  onDelete: (noteId: string) => void;
  onToggleIngredient: (note: ReelNote, ingredientIndex: number) => void;
  onToggleStep: (note: ReelNote, stepNumber: number) => void;
  language: 'fr' | 'en';
}

export const NoteDetailModal: React.FC<NoteDetailModalProps> = ({
  note,
  onClose,
  onToggleFavorite,
  onDelete,
  onToggleIngredient,
  onToggleStep,
  language,
}) => {
  const isFr = language === 'fr';
  const [activeTab, setActiveTab] = useState<'structured' | 'markdown' | 'caption'>('structured');
  const [copied, setCopied] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

  const catConfig = CATEGORIES_CONFIG[note.category] || CATEGORIES_CONFIG.GENERAL;

  const handleCopyMarkdown = async () => {
    try {
      await navigator.clipboard.writeText(note.markdownContent || note.rawCaption);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (e) {
      console.error('Clipboard copy failed:', e);
    }
  };

  const handleShare = async () => {
    if (navigator.share) {
      try {
        await navigator.share({
          title: note.title,
          text: `${note.title}\n\n${note.summary}`,
          url: note.reelUrl,
        });
      } catch (err) {
        // User cancelled share
      }
    } else {
      handleCopyMarkdown();
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-0 sm:p-4 bg-black/80 backdrop-blur-md animate-fadeIn">
      <div className="relative w-full max-w-2xl h-full sm:h-auto sm:max-h-[92vh] bg-[#0E1017] sm:rounded-3xl border border-[#232733] shadow-2xl flex flex-col overflow-hidden">
        {/* Modal Top Bar */}
        <div className="flex items-center justify-between px-4 sm:px-6 py-4 bg-[#14161D]/90 backdrop-blur-md border-b border-[#232733] shrink-0">
          <button
            onClick={onClose}
            className="flex items-center gap-1.5 text-xs font-semibold text-slate-300 hover:text-white bg-[#1C1F2B] hover:bg-[#252A3B] px-3 py-1.5 rounded-full border border-[#282C3D] transition-colors cursor-pointer"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>{isFr ? 'Retour' : 'Back'}</span>
          </button>

          {/* Category Pill */}
          <span
            className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold border ${catConfig.badgeBg}`}
          >
            <span>{catConfig.iconEmoji}</span>
            <span>{isFr ? catConfig.labelFr : catConfig.labelEn}</span>
          </span>

          {/* Quick Actions */}
          <div className="flex items-center gap-1.5">
            <button
              onClick={() => onToggleFavorite(note.id)}
              className={`p-2 rounded-xl border transition-colors cursor-pointer ${
                note.isFavorite
                  ? 'bg-amber-500/15 border-amber-500/40 text-amber-400'
                  : 'bg-[#1C1F2B] border-[#282C3D] text-slate-400 hover:text-white'
              }`}
              title={isFr ? 'Favori' : 'Favorite'}
            >
              <Star className={`w-4 h-4 ${note.isFavorite ? 'fill-amber-400' : ''}`} />
            </button>

            <button
              onClick={handleCopyMarkdown}
              className={`p-2 rounded-xl border transition-colors cursor-pointer ${
                copied
                  ? 'bg-emerald-500/15 border-emerald-500/40 text-emerald-400'
                  : 'bg-[#1C1F2B] border-[#282C3D] text-slate-400 hover:text-white'
              }`}
              title={isFr ? 'Copier Markdown' : 'Copy Markdown'}
            >
              {copied ? <Check className="w-4 h-4" /> : <Copy className="w-4 h-4" />}
            </button>

            <button
              onClick={handleShare}
              className="p-2 rounded-xl bg-[#1C1F2B] border border-[#282C3D] text-slate-400 hover:text-white transition-colors cursor-pointer"
              title={isFr ? 'Partager' : 'Share'}
            >
              <Share2 className="w-4 h-4" />
            </button>

            <a
              href={note.reelUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="p-2 rounded-xl bg-[#1C1F2B] border border-[#282C3D] text-slate-400 hover:text-violet-300 transition-colors"
              title={isFr ? 'Ouvrir sur Instagram' : 'Open on Instagram'}
            >
              <ExternalLink className="w-4 h-4" />
            </a>

            <button
              onClick={() => setShowDeleteConfirm(true)}
              className="p-2 rounded-xl bg-[#1C1F2B] border border-[#282C3D] text-slate-400 hover:text-red-400 transition-colors cursor-pointer"
              title={isFr ? 'Supprimer' : 'Delete'}
            >
              <Trash2 className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* Delete Confirmation Alert Banner */}
        {showDeleteConfirm && (
          <div className="bg-red-950/40 border-b border-red-500/30 px-6 py-3 flex items-center justify-between text-xs">
            <span className="text-red-200">
              {isFr
                ? 'Confirmer la suppression définitive de cette note ?'
                : 'Are you sure you want to permanently delete this note?'}
            </span>
            <div className="flex items-center gap-2">
              <button
                onClick={() => setShowDeleteConfirm(false)}
                className="px-2.5 py-1 rounded bg-slate-800 text-slate-300 hover:bg-slate-700 cursor-pointer"
              >
                {isFr ? 'Annuler' : 'Cancel'}
              </button>
              <button
                onClick={() => {
                  onDelete(note.id);
                  onClose();
                }}
                className="px-2.5 py-1 rounded bg-red-600 text-white font-semibold hover:bg-red-500 cursor-pointer"
              >
                {isFr ? 'Supprimer' : 'Delete'}
              </button>
            </div>
          </div>
        )}

        {/* Modal Scrollable Body */}
        <div className="flex-1 overflow-y-auto px-4 sm:px-6 py-5 space-y-5">
          {/* Title and Author */}
          <div>
            <h1 className="text-xl sm:text-2xl font-black text-white leading-tight tracking-tight mb-1.5">
              {note.title}
            </h1>
            <div className="flex flex-wrap items-center gap-3 text-xs text-slate-400">
              {note.author && (
                <span className="font-semibold text-violet-400">{note.author}</span>
              )}
              <span>•</span>
              <span>Reel #{note.shortcode}</span>
              <span>•</span>
              <span>{new Date(note.createdAt).toLocaleDateString(isFr ? 'fr-FR' : 'en-US')}</span>
            </div>
          </div>

          {/* Quick Metrics Bar: Prep Time / Cook Time / Servings */}
          {(note.structuredData?.prepTime ||
            note.structuredData?.cookTime ||
            note.structuredData?.servings) && (
            <div className="flex flex-wrap items-center gap-2.5 p-3 rounded-2xl bg-[#14161D] border border-[#232733]">
              {note.structuredData.prepTime && (
                <div className="flex items-center gap-1.5 text-xs text-slate-200 bg-[#1C1F2B] px-3 py-1.5 rounded-xl border border-[#282C3D]">
                  <Clock className="w-3.5 h-3.5 text-cyan-400" />
                  <span className="text-slate-400">{isFr ? 'Préparation :' : 'Prep :'}</span>
                  <span className="font-bold text-cyan-300">{note.structuredData.prepTime}</span>
                </div>
              )}
              {note.structuredData.cookTime && (
                <div className="flex items-center gap-1.5 text-xs text-slate-200 bg-[#1C1F2B] px-3 py-1.5 rounded-xl border border-[#282C3D]">
                  <Flame className="w-3.5 h-3.5 text-orange-400" />
                  <span className="text-slate-400">{isFr ? 'Cuisson :' : 'Cook :'}</span>
                  <span className="font-bold text-orange-300">{note.structuredData.cookTime}</span>
                </div>
              )}
              {note.structuredData.servings && (
                <div className="flex items-center gap-1.5 text-xs text-slate-200 bg-[#1C1F2B] px-3 py-1.5 rounded-xl border border-[#282C3D]">
                  <Users className="w-3.5 h-3.5 text-emerald-400" />
                  <span className="text-slate-400">{isFr ? 'Portions :' : 'Servings :'}</span>
                  <span className="font-bold text-emerald-300">{note.structuredData.servings}</span>
                </div>
              )}
            </div>
          )}

          {/* View Tabs Selector */}
          <div className="flex items-center gap-2 p-1 bg-[#14161D] rounded-xl border border-[#232733] w-fit text-xs font-semibold">
            <button
              onClick={() => setActiveTab('structured')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg transition-all cursor-pointer ${
                activeTab === 'structured'
                  ? 'bg-violet-600 text-white shadow-sm'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              <Sparkles className="w-3.5 h-3.5" />
              <span>{isFr ? 'Note Interactive' : 'Interactive Note'}</span>
            </button>

            <button
              onClick={() => setActiveTab('markdown')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg transition-all cursor-pointer ${
                activeTab === 'markdown'
                  ? 'bg-violet-600 text-white shadow-sm'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              <FileText className="w-3.5 h-3.5" />
              <span>Markdown</span>
            </button>

            <button
              onClick={() => setActiveTab('caption')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg transition-all cursor-pointer ${
                activeTab === 'caption'
                  ? 'bg-violet-600 text-white shadow-sm'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              <AlignLeft className="w-3.5 h-3.5" />
              <span>{isFr ? 'Légende originale' : 'Original Caption'}</span>
            </button>
          </div>

          {/* TAB 1: Structured View */}
          {activeTab === 'structured' && (
            <div className="space-y-5 animate-fadeIn">
              {/* Summary Card */}
              {note.summary && (
                <div className="bg-[#14161D] p-4 rounded-2xl border border-[#232733]">
                  <h3 className="text-xs font-bold text-violet-400 uppercase tracking-wider mb-1.5 flex items-center gap-1.5">
                    <CheckCircle2 className="w-3.5 h-3.5" />
                    <span>{isFr ? 'En Bref' : 'TL;DR Summary'}</span>
                  </h3>
                  <p className="text-sm text-slate-200 leading-relaxed font-medium">
                    {note.summary}
                  </p>
                </div>
              )}

              {/* Interactive Recipe Checklist */}
              <RecipeChecklist
                ingredients={note.structuredData.ingredients}
                onToggleIngredient={idx => onToggleIngredient(note, idx)}
                language={language}
              />

              {/* Step By Step List */}
              <StepList
                steps={note.structuredData.steps}
                onToggleStep={stepNum => onToggleStep(note, stepNum)}
                language={language}
              />

              {/* Key Takeaways */}
              {note.structuredData.keyTakeaways && note.structuredData.keyTakeaways.length > 0 && (
                <div className="bg-[#14161D] rounded-2xl border border-[#232733] p-4.5">
                  <h3 className="font-bold text-sm text-slate-100 mb-3 flex items-center gap-2">
                    <span className="w-2 h-2 rounded-full bg-cyan-400" />
                    <span>{isFr ? 'Points Clés à Retenir' : 'Key Takeaways'}</span>
                  </h3>
                  <ul className="space-y-2">
                    {note.structuredData.keyTakeaways.map((pt, i) => (
                      <li key={i} className="flex items-start gap-2.5 text-sm text-slate-300">
                        <span className="text-cyan-400 font-bold">•</span>
                        <span>{pt}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              )}

              {/* Tips */}
              {note.structuredData.tips && note.structuredData.tips.length > 0 && (
                <div className="bg-amber-950/20 border border-amber-500/25 rounded-2xl p-4.5">
                  <h3 className="font-bold text-sm text-amber-300 mb-2.5 flex items-center gap-2">
                    <Lightbulb className="w-4 h-4 text-amber-400" />
                    <span>{isFr ? 'Astuces du Chef / Pro Tips' : 'Pro Tips'}</span>
                  </h3>
                  <ul className="space-y-1.5">
                    {note.structuredData.tips.map((tip, i) => (
                      <li key={i} className="text-xs text-amber-200/90 leading-relaxed">
                        • {tip}
                      </li>
                    ))}
                  </ul>
                </div>
              )}

              {/* Tags */}
              {note.tags && note.tags.length > 0 && (
                <div className="flex flex-wrap items-center gap-1.5 pt-2">
                  <span className="text-xs text-slate-500 font-medium mr-1">Tags :</span>
                  {note.tags.map(tag => (
                    <span
                      key={tag}
                      className="text-xs text-slate-400 bg-[#14161D] border border-[#232733] px-2.5 py-1 rounded-full font-mono"
                    >
                      #{tag}
                    </span>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* TAB 2: Markdown View */}
          {activeTab === 'markdown' && (
            <div className="space-y-3 animate-fadeIn">
              <div className="flex items-center justify-between">
                <span className="text-xs text-slate-400">
                  {isFr ? 'Texte au format Markdown prêt à copier' : 'Ready-to-copy Markdown note'}
                </span>
                <button
                  onClick={handleCopyMarkdown}
                  className="flex items-center gap-1.5 text-xs font-semibold px-3 py-1.5 rounded-lg bg-violet-600 hover:bg-violet-500 text-white cursor-pointer transition-colors"
                >
                  {copied ? <Check className="w-3.5 h-3.5" /> : <Copy className="w-3.5 h-3.5" />}
                  <span>{copied ? (isFr ? 'Copié !' : 'Copied !') : (isFr ? 'Copier tout' : 'Copy all')}</span>
                </button>
              </div>

              <div className="bg-[#090A0E] rounded-2xl border border-[#232733] p-4 text-xs font-mono text-slate-300 whitespace-pre-wrap leading-relaxed select-all overflow-x-auto">
                {note.markdownContent || note.rawCaption}
              </div>
            </div>
          )}

          {/* TAB 3: Original Reel Caption */}
          {activeTab === 'caption' && (
            <div className="space-y-3 animate-fadeIn">
              <span className="text-xs text-slate-400">
                {isFr ? 'Texte brut extrait depuis Instagram' : 'Raw text extracted from Instagram'}
              </span>
              <div className="bg-[#090A0E] rounded-2xl border border-[#232733] p-4 text-xs text-slate-300 whitespace-pre-wrap leading-relaxed">
                {note.rawCaption || (isFr ? 'Aucune légende brute enregistrée.' : 'No caption recorded.')}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
