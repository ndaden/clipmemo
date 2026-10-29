import React from 'react';
import { Film, CheckCircle, Sparkles, ChefHat, Dumbbell, Zap, ArrowRight, X } from 'lucide-react';

interface OnboardingDialogProps {
  isOpen: boolean;
  onClose: () => void;
  language: 'fr' | 'en';
}

export const OnboardingDialog: React.FC<OnboardingDialogProps> = ({
  isOpen,
  onClose,
  language,
}) => {
  if (!isOpen) return null;
  const isFr = language === 'fr';

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-fadeIn">
      <div className="relative w-full max-w-md bg-[#0E1017] rounded-3xl border border-[#232733] shadow-2xl overflow-hidden p-6 text-center">
        {/* Close icon */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 p-1.5 rounded-lg text-slate-500 hover:text-white hover:bg-[#1C1F2B] transition-colors cursor-pointer"
        >
          <X className="w-5 h-5" />
        </button>

        {/* Hero badge */}
        <div className="w-16 h-16 mx-auto mb-4 rounded-2xl bg-gradient-to-tr from-violet-600 via-purple-500 to-cyan-400 flex items-center justify-center shadow-xl shadow-violet-500/30 text-white">
          <Film className="w-8 h-8" />
        </div>

        <h2 className="text-xl font-black text-white mb-2 tracking-tight">
          {isFr ? 'Bienvenue sur ReelNotes !' : 'Welcome to ReelNotes!'}
        </h2>
        <p className="text-xs text-slate-400 leading-relaxed mb-6">
          {isFr
            ? 'Ne perdez plus jamais une recette, un entraînement ou une astuce vue dans vos Reels Instagram.'
            : 'Never lose a recipe, workout routine, or tip you saw in your Instagram Reels again.'}
        </p>

        {/* 3 Step workflow */}
        <div className="space-y-3 text-left mb-6">
          <div className="flex items-start gap-3 p-3 rounded-2xl bg-[#14161D] border border-[#232733]">
            <div className="w-7 h-7 rounded-xl bg-violet-500/20 text-violet-400 flex items-center justify-center font-bold text-xs shrink-0 mt-0.5">
              1
            </div>
            <div>
              <h4 className="text-xs font-bold text-slate-200">
                {isFr ? 'Trouvez un Reel Instagram' : 'Find an Instagram Reel'}
              </h4>
              <p className="text-[11px] text-slate-400">
                {isFr
                  ? 'Recette gourmande, séance fitness, tutoriel express...'
                  : 'Delicious recipe, workout session, fast tutorial...'}
              </p>
            </div>
          </div>

          <div className="flex items-start gap-3 p-3 rounded-2xl bg-[#14161D] border border-[#232733]">
            <div className="w-7 h-7 rounded-xl bg-cyan-500/20 text-cyan-400 flex items-center justify-center font-bold text-xs shrink-0 mt-0.5">
              2
            </div>
            <div>
              <h4 className="text-xs font-bold text-slate-200">
                {isFr ? 'Collez le lien ou la légende' : 'Paste link or caption'}
              </h4>
              <p className="text-[11px] text-slate-400">
                {isFr
                  ? 'Notre IA extrait automatiquement toutes les données utiles'
                  : 'Our AI and heuristics automatically extract key ingredients and steps'}
              </p>
            </div>
          </div>

          <div className="flex items-start gap-3 p-3 rounded-2xl bg-[#14161D] border border-[#232733]">
            <div className="w-7 h-7 rounded-xl bg-emerald-500/20 text-emerald-400 flex items-center justify-center font-bold text-xs shrink-0 mt-0.5">
              3
            </div>
            <div>
              <h4 className="text-xs font-bold text-slate-200">
                {isFr ? 'Cochez & cuisinez / pratiquez' : 'Check off & take action'}
              </h4>
              <p className="text-[11px] text-slate-400">
                {isFr
                  ? 'Une checklist interactive pour vos courses et votre préparation'
                  : 'Interactive shopping checklist and step-by-step progress tracker'}
              </p>
            </div>
          </div>
        </div>

        {/* CTA Button */}
        <button
          onClick={onClose}
          className="w-full py-3 px-4 rounded-xl font-bold text-xs bg-gradient-to-r from-violet-600 via-purple-600 to-cyan-600 text-white shadow-lg shadow-violet-500/30 hover:opacity-95 transition-all flex items-center justify-center gap-2 cursor-pointer"
        >
          <span>{isFr ? "C'est parti !" : 'Get Started!'}</span>
          <ArrowRight className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
};
