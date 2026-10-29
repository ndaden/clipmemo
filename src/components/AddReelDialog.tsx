import React, { useState } from 'react';
import {
  X,
  Link as LinkIcon,
  Sparkles,
  FileText,
  Bookmark,
  Cpu,
  Zap,
  ArrowRight,
  AlertCircle,
} from 'lucide-react';
import { UrlParser } from '../services/urlParser';
import { GeminiSummarizer } from '../services/geminiSummarizer';
import { OfflineHeuristicExtractor } from '../services/offlineHeuristicExtractor';
import { ReelNote } from '../types/note';

interface AddReelDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onNoteCreated: (note: ReelNote) => void;
  language: 'fr' | 'en';
  geminiApiKey?: string;
  onStartProcessing: (statusText: string) => void;
  onEndProcessing: () => void;
}

const SAMPLE_REELS = [
  {
    title: '🥑 Guacamole Mexicain Traditionnel',
    category: 'Recette',
    shortcode: 'C7m9Xy1Az',
    author: '@chef_carlos',
    url: 'https://www.instagram.com/reel/C7m9Xy1Az/',
    caption: `Le secret du vrai guacamole mexicain de grand-mère 🥑🇲🇽
Pas de crème, pas d'épices chimiques, juste des produits frais !

Ingrédients :
- 3 avocats Hass bien mûrs
- 1 jus de citron vert frais
- 1/2 oignon rouge émincé très finement
- 1 tomate Roma épépinée et coupée en dés
- 1 petit piment jalapeño haché
- 1 petit bouquet de coriandre fraîche
- 1 pincée de sel de Guérande

Préparation :
1. Dans un molcajete ou un bol en pierre, écraser l'oignon rouge, le piment, un peu de coriandre et le sel pour extraire les arômes.
2. Couper les avocats en deux, retirer le noyau et ajouter la chair.
3. Écraser grossièrement à la fourchette pour garder de la texture (morceaux fondants).
4. Ajouter le jus de citron vert et les dés de tomates.
5. Mélanger délicatement et parsemer de coriandre fraîche. Servir avec des totopos chauds !

#guacamole #mexicanfood #recette #avocado #apero`,
  },
  {
    title: '🔋 3 Réglages Cachés pour Doubler la Batterie iPhone',
    category: 'Astuce',
    shortcode: 'C3kP819Zq',
    author: '@tech_astuces',
    url: 'https://www.instagram.com/reel/C3kP819Zq/',
    caption: `Double l'autonomie de ton iPhone avec ces 3 réglages secrets 📱⚡

1. Désactive l'actualisation en arrière-plan inutile : Va dans Réglages > Général > Actualiser en arrière-plan et désactive toutes les applis que tu n'utilises pas en temps réel.
2. Coupe la localisation systématique : Réglages > Confidentialité > Service de localisation. Mets "Lorsque l'app est active" partout.
3. Désactive le retour haptique du clavier si tu tapes beaucoup (Réglages > Sons et vibrations > Retour clavier).

#iphone #astuce #techhack #battery #apple #productivite`,
  },
  {
    title: '💪 Abdos Béton 10 Min Sans Matériel',
    category: 'Sport',
    shortcode: 'C2xY19Lp0',
    author: '@fit_sarah',
    url: 'https://www.instagram.com/reel/C2xY19Lp0/',
    caption: `Routine Abdos Brûle-Graisses 10 minutes express 🔥
Circuit 45s effort / 15s repos. 2 séries complètes.

Exercices :
1. Deadbug contrôlé (garder le bas du dos plaqué au sol)
2. Hollow body hold (gainage banane dynamique)
3. Russian twists avec jambes décollées
4. Planche latérale avec élévation de hanche
5. Crunchs bicyclette en rythme

Points clés :
- Ne tire jamais sur la nuque avec tes mains
- Expire au moment de la contraction

#abdos #fitness #homeworkout #workout #coretraining`,
  },
];

export const AddReelDialog: React.FC<AddReelDialogProps> = ({
  isOpen,
  onClose,
  onNoteCreated,
  language,
  geminiApiKey,
  onStartProcessing,
  onEndProcessing,
}) => {
  const isFr = language === 'fr';
  const [activeTab, setActiveTab] = useState<'url' | 'manual' | 'samples'>('url');
  const [inputUrl, setInputUrl] = useState('');
  const [manualCaption, setManualCaption] = useState('');
  const [authorInput, setAuthorInput] = useState('');
  const [useAi, setUseAi] = useState(true);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleProcess = async (urlText: string, captionText: string, authorOverride?: string) => {
    setErrorMsg(null);
    const cleanedUrl = urlText.trim();
    const cleanedCaption = captionText.trim();

    if (!cleanedUrl && !cleanedCaption) {
      setErrorMsg(
        isFr
          ? 'Veuillez saisir une URL de Reel ou coller une légende.'
          : 'Please enter an Instagram Reel URL or paste caption text.'
      );
      return;
    }

    // Extract Reel URL info
    const parsedReel = UrlParser.extractReelInfo(cleanedUrl);
    const shortcode = parsedReel?.shortcode || `reel-${Math.random().toString(36).substr(2, 6)}`;
    const reelUrl = parsedReel?.cleanUrl || (cleanedUrl.startsWith('http') ? cleanedUrl : `https://www.instagram.com/reel/${shortcode}/`);

    // Extract author
    const author =
      authorOverride ||
      UrlParser.extractAuthorFromSharedText(cleanedUrl) ||
      UrlParser.extractAuthorFromSharedText(cleanedCaption);

    // Extract caption
    const effectiveCaption =
      cleanedCaption ||
      UrlParser.extractCaptionFromSharedText(cleanedUrl) ||
      `Instagram Reel ${shortcode}`;

    onStartProcessing(
      useAi
        ? (isFr ? 'IA Gemini analyse la vidéo & la légende...' : 'Gemini AI is analyzing reel & caption...')
        : (isFr ? 'Extraction rapide par heuristiques...' : 'Fast heuristic structuring...')
    );

    try {
      let note: ReelNote;

      if (useAi) {
        note = await GeminiSummarizer.summarize({
          caption: effectiveCaption,
          reelUrl,
          shortcode,
          author,
          preferredLanguage: language,
          apiKey: geminiApiKey,
        });
      } else {
        note = OfflineHeuristicExtractor.extract(
          effectiveCaption,
          reelUrl,
          shortcode,
          author,
          null,
          language
        );
      }

      onEndProcessing();
      onNoteCreated(note);
      onClose();
      // Reset form
      setInputUrl('');
      setManualCaption('');
      setAuthorInput('');
    } catch (err: any) {
      onEndProcessing();
      console.error('Error extracting reel:', err);
      setErrorMsg(err?.message || (isFr ? 'Erreur lors du traitement du Reel.' : 'Failed to process Reel.'));
    }
  };

  const handleSelectSample = (sample: typeof SAMPLE_REELS[0]) => {
    handleProcess(sample.url, sample.caption, sample.author);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-black/80 backdrop-blur-md animate-fadeIn">
      <div className="relative w-full max-w-lg bg-[#0E1017] rounded-3xl border border-[#232733] shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-[#232733] bg-[#14161D]">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-xl bg-violet-600/20 border border-violet-500/40 flex items-center justify-center text-violet-400">
              <Sparkles className="w-4 h-4" />
            </div>
            <div>
              <h2 className="font-bold text-base text-white">
                {isFr ? 'Nouveau Reel' : 'New Reel Note'}
              </h2>
              <p className="text-[11px] text-slate-400">
                {isFr ? 'Convertir en note actionnable' : 'Transform into structured note'}
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-[#1C1F2B] transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Tab switcher */}
        <div className="flex border-b border-[#232733] bg-[#11131A] text-xs font-semibold px-4 pt-2 gap-2">
          <button
            onClick={() => setActiveTab('url')}
            className={`pb-2.5 px-3 border-b-2 transition-all cursor-pointer flex items-center gap-1.5 ${
              activeTab === 'url'
                ? 'border-violet-500 text-violet-400'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            <LinkIcon className="w-3.5 h-3.5" />
            <span>{isFr ? 'Lien Instagram' : 'Instagram URL'}</span>
          </button>

          <button
            onClick={() => setActiveTab('manual')}
            className={`pb-2.5 px-3 border-b-2 transition-all cursor-pointer flex items-center gap-1.5 ${
              activeTab === 'manual'
                ? 'border-violet-500 text-violet-400'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            <FileText className="w-3.5 h-3.5" />
            <span>{isFr ? 'Coller Légende' : 'Paste Caption'}</span>
          </button>

          <button
            onClick={() => setActiveTab('samples')}
            className={`pb-2.5 px-3 border-b-2 transition-all cursor-pointer flex items-center gap-1.5 ${
              activeTab === 'samples'
                ? 'border-violet-500 text-violet-400'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            <Bookmark className="w-3.5 h-3.5" />
            <span>{isFr ? 'Exemples ⚡' : 'Sample Reels'}</span>
          </button>
        </div>

        {/* Error message */}
        {errorMsg && (
          <div className="mx-6 mt-4 p-3 rounded-xl bg-red-950/40 border border-red-500/30 text-xs text-red-300 flex items-start gap-2">
            <AlertCircle className="w-4 h-4 shrink-0 text-red-400 mt-0.5" />
            <span>{errorMsg}</span>
          </div>
        )}

        {/* Content body */}
        <div className="p-6 overflow-y-auto space-y-4 flex-1">
          {/* TAB 1: Instagram URL */}
          {activeTab === 'url' && (
            <div className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-slate-300 mb-1.5">
                  {isFr ? 'Lien du Reel Instagram ou texte partagé' : 'Instagram Reel URL or shared text'}
                </label>
                <div className="relative">
                  <input
                    type="text"
                    value={inputUrl}
                    onChange={e => setInputUrl(e.target.value)}
                    placeholder="https://www.instagram.com/reel/C3zYx7oI8Pq/..."
                    className="w-full bg-[#14161D] border border-[#232733] focus:border-violet-500 rounded-xl px-4 py-2.5 text-xs text-white placeholder-slate-500 outline-none transition-colors"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">
                  {isFr ? 'Légende additionnelle (facultatif)' : 'Additional caption / context (optional)'}
                </label>
                <textarea
                  rows={3}
                  value={manualCaption}
                  onChange={e => setManualCaption(e.target.value)}
                  placeholder={
                    isFr
                      ? "Collez ici les ingrédients ou le texte de la publication si le Reel est privé ou restreint..."
                      : "Paste ingredients or caption text here if the reel is private or restricted..."
                  }
                  className="w-full bg-[#14161D] border border-[#232733] focus:border-violet-500 rounded-xl p-3 text-xs text-white placeholder-slate-600 outline-none transition-colors resize-none"
                />
              </div>
            </div>
          )}

          {/* TAB 2: Manual Caption */}
          {activeTab === 'manual' && (
            <div className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-slate-300 mb-1.5">
                  {isFr ? 'Légende ou texte de la vidéo' : 'Video caption or transcript'}
                </label>
                <textarea
                  rows={6}
                  value={manualCaption}
                  onChange={e => setManualCaption(e.target.value)}
                  placeholder={
                    isFr
                      ? "Collez la recette, le tutoriel ou la description du Reel..."
                      : "Paste recipe, tutorial, or workout instructions here..."
                  }
                  className="w-full bg-[#14161D] border border-[#232733] focus:border-violet-500 rounded-xl p-3 text-xs text-white placeholder-slate-600 outline-none transition-colors resize-none"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">
                  {isFr ? 'Auteur / Créateur (ex: @chef_matteo)' : 'Creator / Author (e.g. @chef_matteo)'}
                </label>
                <input
                  type="text"
                  value={authorInput}
                  onChange={e => setAuthorInput(e.target.value)}
                  placeholder="@username"
                  className="w-full bg-[#14161D] border border-[#232733] focus:border-violet-500 rounded-xl px-3 py-2 text-xs text-white placeholder-slate-600 outline-none transition-colors"
                />
              </div>
            </div>
          )}

          {/* TAB 3: Popular Samples */}
          {activeTab === 'samples' && (
            <div className="space-y-3">
              <p className="text-xs text-slate-400">
                {isFr
                  ? 'Cliquez sur un exemple prêt à l’emploi pour tester la transformation en 1 seconde :'
                  : 'Click on a ready-to-test sample to see the instant structuring in 1 second:'}
              </p>
              <div className="space-y-2.5">
                {SAMPLE_REELS.map((sample, idx) => (
                  <div
                    key={idx}
                    onClick={() => handleSelectSample(sample)}
                    className="p-3 rounded-2xl bg-[#14161D] hover:bg-[#1C1F2B] border border-[#232733] hover:border-violet-500/50 cursor-pointer transition-all flex items-center justify-between group"
                  >
                    <div>
                      <div className="flex items-center gap-2 mb-1">
                        <span className="text-xs font-bold text-slate-200 group-hover:text-violet-300 transition-colors">
                          {sample.title}
                        </span>
                        <span className="text-[10px] px-2 py-0.5 rounded-full bg-violet-950/40 border border-violet-500/30 text-violet-400 font-semibold">
                          {sample.category}
                        </span>
                      </div>
                      <p className="text-[11px] text-slate-400 line-clamp-1">
                        {sample.author} • Reel #{sample.shortcode}
                      </p>
                    </div>
                    <ArrowRight className="w-4 h-4 text-slate-500 group-hover:text-violet-400 group-hover:translate-x-0.5 transition-all shrink-0 ml-2" />
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* AI vs Heuristics mode switch */}
          {activeTab !== 'samples' && (
            <div className="pt-2 border-t border-[#232733] flex items-center justify-between">
              <div className="flex items-center gap-2">
                {useAi ? (
                  <Cpu className="w-4 h-4 text-violet-400" />
                ) : (
                  <Zap className="w-4 h-4 text-amber-400" />
                )}
                <div className="flex flex-col">
                  <span className="text-xs font-bold text-slate-200">
                    {useAi
                      ? (isFr ? 'Mode IA Gemini (Structuration intelligente)' : 'Gemini AI Mode (Smart Extraction)')
                      : (isFr ? 'Mode Hors-ligne (Heuristiques rapides)' : 'Offline Heuristics (Instant)')}
                  </span>
                  <span className="text-[10px] text-slate-400">
                    {useAi
                      ? (isFr ? 'Extraction automatique ingrédients, étapes & métriques' : 'Detects ingredients, steps, and timings')
                      : (isFr ? '100% local sans appel réseau' : '100% offline rule-based parser')}
                  </span>
                </div>
              </div>
              <button
                type="button"
                onClick={() => setUseAi(!useAi)}
                className={`relative inline-flex h-5 w-9 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none ${
                  useAi ? 'bg-violet-600' : 'bg-slate-700'
                }`}
              >
                <span
                  className={`pointer-events-none inline-block h-4 w-4 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out ${
                    useAi ? 'translate-x-4' : 'translate-x-0'
                  }`}
                />
              </button>
            </div>
          )}
        </div>

        {/* Footer actions */}
        {activeTab !== 'samples' && (
          <div className="px-6 py-4 border-t border-[#232733] bg-[#14161D] flex items-center justify-end gap-3">
            <button
              onClick={onClose}
              className="px-4 py-2 rounded-xl text-xs font-semibold text-slate-400 hover:text-white bg-transparent hover:bg-[#1C1F2B] transition-colors cursor-pointer"
            >
              {isFr ? 'Annuler' : 'Cancel'}
            </button>
            <button
              onClick={() => handleProcess(inputUrl, manualCaption, authorInput)}
              className="flex items-center gap-2 px-5 py-2.5 rounded-xl text-xs font-bold bg-gradient-to-r from-violet-600 to-purple-600 hover:from-violet-500 hover:to-purple-500 text-white shadow-lg shadow-violet-500/25 transition-all cursor-pointer"
            >
              <Sparkles className="w-3.5 h-3.5" />
              <span>{isFr ? 'Extraire & Structurer' : 'Extract & Structure'}</span>
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
