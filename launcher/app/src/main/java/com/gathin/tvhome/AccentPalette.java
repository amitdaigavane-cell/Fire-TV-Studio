package com.gathin.tvhome;

/** Small color histogram, softened for readable highlights on dark surfaces. */
final class AccentPalette {
    static int choose(int[] pixels,int fallback){
        long[] score=new long[64],red=new long[64],green=new long[64],blue=new long[64];int[] count=new int[64];
        for(int color:pixels){if((color>>>24)<128)continue;int r=(color>>16)&255,g=(color>>8)&255,b=color&255;
            int range=Math.max(r,Math.max(g,b))-Math.min(r,Math.min(g,b));if(range<24)continue;
            int bin=(r>>6)*16+(g>>6)*4+(b>>6);score[bin]+=range;red[bin]+=r;green[bin]+=g;blue[bin]+=b;count[bin]++;
        }
        int best=0;for(int i=1;i<64;i++)if(score[i]>score[best])best=i;
        if(count[best]==0)return fallback;
        int r=(int)(red[best]/count[best]),g=(int)(green[best]/count[best]),b=(int)(blue[best]/count[best]);
        return 0xff000000|((r+255)/2<<16)|((g+255)/2<<8)|(b+255)/2;
    }
}
