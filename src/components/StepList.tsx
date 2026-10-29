import React from 'react';
import { Check, ListOrdered } from 'lucide-react';
import { StepItem } from '../types/note';

interface StepListProps {
  steps: StepItem[];
  onToggleStep: (stepNumber: number) => void;
  language: 'fr' | 'en';
}

export const StepList: React.FC<StepListProps> = ({ steps, onToggleStep, language }) => {
  const isFr = language === 'fr';

  if (!steps || steps.length === 0) return null;

  return (
    <div className="bg-[#14161D] rounded-2xl border border-[#232733] p-4.5 mb-5 shadow-lg">
      <div className="flex items-center gap-2 mb-4">
        <div className="w-7 h-7 rounded-lg bg-violet-500/15 border border-violet-500/30 flex items-center justify-center text-violet-400">
          <ListOrdered className="w-4 h-4" />
        </div>
        <h3 className="font-bold text-sm text-slate-100">
          {isFr ? 'Étapes & Préparation' : 'Step-by-Step Instructions'}
        </h3>
      </div>

      <div className="relative pl-3 space-y-4 before:absolute before:left-[21px] before:top-3 before:bottom-3 before:w-[2px] before:bg-slate-800">
        {steps.map(step => {
          const isDone = !!step.isDone;

          return (
            <div
              key={step.stepNumber}
              onClick={() => onToggleStep(step.stepNumber)}
              className="relative flex items-start gap-3.5 group cursor-pointer"
            >
              {/* Step indicator node */}
              <div
                className={`relative z-10 w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold transition-all shrink-0 ${
                  isDone
                    ? 'bg-violet-600 text-white shadow-md shadow-violet-500/40 ring-4 ring-[#14161D]'
                    : 'bg-[#1C1F2B] border border-slate-600 text-slate-300 group-hover:border-violet-400 ring-4 ring-[#14161D]'
                }`}
              >
                {isDone ? <Check className="w-3.5 h-3.5 stroke-[3]" /> : step.stepNumber}
              </div>

              {/* Step text content */}
              <div
                className={`flex-1 p-3 rounded-xl border transition-all text-sm leading-relaxed ${
                  isDone
                    ? 'bg-violet-950/20 border-violet-500/20 text-slate-400 line-through'
                    : 'bg-[#1C1F2B]/70 group-hover:bg-[#1C1F2B] border-[#232733] text-slate-200'
                }`}
              >
                {step.instruction}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
