import React from 'react';
import { Loader2, Sparkles, Film } from 'lucide-react';

interface ProcessingOverlayProps {
  isProcessing: boolean;
  statusText: string;
}

export const ProcessingOverlay: React.FC<ProcessingOverlayProps> = ({
  isProcessing,
  statusText,
}) => {
  if (!isProcessing) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/85 backdrop-blur-md animate-fadeIn">
      <div className="relative bg-[#14161D] rounded-3xl border border-violet-500/40 p-7 max-w-sm w-full text-center shadow-2xl shadow-violet-950/50 flex flex-col items-center">
        {/* Animated glowing spinner */}
        <div className="relative w-16 h-16 mb-5">
          <div className="absolute inset-0 rounded-2xl bg-gradient-to-tr from-violet-600 to-cyan-500 animate-pulse blur-md opacity-60" />
          <div className="relative w-16 h-16 rounded-2xl bg-[#1C1F2B] border border-violet-500/50 flex items-center justify-center text-violet-400">
            <Film className="w-8 h-8 animate-bounce" />
          </div>
        </div>

        <h3 className="font-extrabold text-base text-white mb-2 flex items-center gap-2">
          <Sparkles className="w-4 h-4 text-violet-400 animate-spin" />
          <span>Extraction en cours...</span>
        </h3>

        <p className="text-xs text-slate-300 leading-relaxed font-medium">
          {statusText || 'Analyse du contenu et structuration de la fiche...'}
        </p>

        <div className="w-full bg-[#1C1F2B] h-1.5 rounded-full overflow-hidden mt-5">
          <div className="bg-gradient-to-r from-violet-500 via-purple-400 to-cyan-400 h-full w-2/3 animate-[pulse_1s_ease-in-out_infinite]" />
        </div>
      </div>
    </div>
  );
};
