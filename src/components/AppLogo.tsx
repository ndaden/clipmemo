import React from 'react';
import { Film } from 'lucide-react';

interface AppLogoProps {
  className?: string;
  size?: 'sm' | 'md' | 'lg';
}

export const AppLogo: React.FC<AppLogoProps> = ({ className = '', size = 'md' }) => {
  const badgeSizes = {
    sm: 'w-7 h-7 rounded-lg text-sm',
    md: 'w-9 h-9 rounded-xl text-base',
    lg: 'w-12 h-12 rounded-2xl text-xl',
  };

  const textSizes = {
    sm: 'text-lg',
    md: 'text-xl',
    lg: 'text-2xl',
  };

  return (
    <div className={`flex items-center gap-2.5 ${className}`}>
      <div
        className={`${badgeSizes[size]} bg-gradient-to-tr from-violet-600 via-purple-500 to-cyan-400 flex items-center justify-center shadow-lg shadow-violet-500/25 shrink-0 text-white font-black`}
      >
        <Film className={size === 'sm' ? 'w-3.5 h-3.5' : size === 'lg' ? 'w-6 h-6' : 'w-4.5 h-4.5'} />
      </div>
      <div className="flex flex-col leading-none">
        <span
          className={`${textSizes[size]} font-extrabold tracking-tight bg-gradient-to-r from-white via-slate-100 to-violet-300 bg-clip-text text-transparent`}
        >
          ReelNotes
        </span>
        <span className="text-[10px] text-violet-400/80 font-medium tracking-wider uppercase mt-0.5">
          ClipMemo AI
        </span>
      </div>
    </div>
  );
};
