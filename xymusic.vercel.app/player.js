// ============================================================
// XMUSIC - CORE PLAYER (FULL)
// ============================================================
const API={search:'/api/search',artist:'/api/artist',suggest:'/api/suggest',lyrics:'/api/lyrics'};
const FI='data:image/svg+xml,'+encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="300" height="300"><rect width="300" height="300" fill="%232a2a2a"/><text x="150" y="150" text-anchor="middle" dy=".3em" font-size="50" fill="%236b7280">🎵</text></svg>');
const S={ht:[],sr:[],ar:[],sq:'',filter:'all',ct:null,pl:[],pi:-1,ps:'',ip:false,il:false,rm:'off',autoNext:true,yp:null,yr:false,iv:null,pt:0,pd:0,at:'home',ld:{type:'none',lines:[]},cli:-1,lo:false};
function fm(s){if(isNaN(s))return"0:00";const m=Math.floor(s/60),se=Math.floor(s%60);return m+':'+(se<10?'0':'')+se;}
function es(t){if(!t)return'';const d=document.createElement('div');d.textContent=t;return d.innerHTML;}
function cn(t){if(!t)return'Unknown';return t.replace(/[^\x20-\x7E\xA0-\xFF\u0100-\uFFFF]/g,'').replace(/\s*-\s*Topic$/i,'').trim()||'Unknown';}
function gid(id){return document.getElementById(id);}

function updateOG(title,image){
    var t=document.querySelector('meta[property="og:title"]');if(!t){t=document.createElement('meta');t.setAttribute('property','og:title');document.head.appendChild(t);}t.setAttribute('content',title+' | Xmusic');
    var i=document.querySelector('meta[property="og:image"]');if(!i){i=document.createElement('meta');i.setAttribute('property','og:image');document.head.appendChild(i);}i.setAttribute('content',image||FI);
    document.title=title+' - Xmusic';
}

const yt=document.createElement('script');yt.src="https://www.youtube.com/iframe_api";document.head.appendChild(yt);
function onYouTubeIframeAPIReady(){S.yp=new YT.Player('yt-player',{height:'0',width:'0',playerVars:{autoplay:1,controls:0,enablejsapi:1,playsinline:1},events:{onReady:function(){S.yr=true;},onStateChange:ys}});}
function ys(e){if(e.data===1){S.ip=true;S.il=false;UB();SP();}else if(e.data===2){S.ip=false;UB();ST();}else if(e.data===0){ST();if(S.rm==='one'){S.yp.seekTo(0);S.yp.playVideo();}else if(S.autoNext){NX();}}else if(e.data===3){S.il=true;UB();}}

function SP(){ST();S.iv=setInterval(function(){if(S.yp&&S.yr&&S.ip){S.pt=S.yp.getCurrentTime()||0;S.pd=S.yp.getDuration()||0;var p=S.pd>0?(S.pt/S.pd)*100:0;var mp=gid('mini-progress'),fp=gid('full-progress'),sb=gid('seek-bar'),tc=gid('time-curr'),td=gid('time-dur');if(mp)mp.style.width=p+'%';if(fp)fp.style.width=p+'%';if(sb)sb.value=p;if(tc)tc.innerText=fm(S.pt);if(td)td.innerText=fm(S.pd);ULH(S.pt);}},200);}
function ST(){if(S.iv){clearInterval(S.iv);S.iv=null;}}

function UB(){
    var mi=gid('mini-play-btn'),fu=gid('full-play-btn');
    if(!mi||!fu)return;
    if(S.il){mi.innerHTML='<div class="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>';fu.innerHTML='<div class="w-6 h-6 border-3 border-black border-t-transparent rounded-full animate-spin"></div>';}
    else if(S.ip){mi.innerHTML='<i data-lucide="pause" class="w-6 h-6 fill-current"></i>';fu.innerHTML='<i data-lucide="pause" class="w-7 h-7 fill-current"></i>';}
    else{mi.innerHTML='<i data-lucide="play" class="w-6 h-6 fill-current"></i>';fu.innerHTML='<i data-lucide="play" class="w-7 h-7 fill-current ml-0.5"></i>';}
    lucide.createIcons();
}

function UU(){if(!S.ct)return;var mc=gid('mini-cover'),mt=gid('mini-title'),ma=gid('mini-artist'),fc=gid('full-cover'),ft=gid('full-title'),fa=gid('full-artist'),fh=gid('full-header-artist'),fb=gid('full-bg-blur');if(mc)mc.src=S.ct.cover;if(mt)mt.innerText=S.ct.title;if(ma)ma.innerText=S.ct.artist;if(fc)fc.src=S.ct.cover;if(ft)ft.innerText=S.ct.title;if(fa)fa.innerText=S.ct.artist;if(fh)fh.innerText=S.ct.artist;if(fb)fb.src=S.ct.cover;updateOG(S.ct.title,S.ct.cover);}

function PK(s,i){var l=[];if(s==='home1')l=S.ht.slice(0,6);else if(s==='home2')l=S.ht.slice(6,12);else if(s==='search')l=S.sr;else if(s==='playlist')l=S.pl;if(!l[i])return;S.ps=s;S.pl=l;S.pi=i;S.ct=l[i];var url=location.origin+'/?play='+S.ct.videoId;history.pushState({},'',url);UU();MP.show();S.il=true;UB();if(S.yp&&S.yr&&S.ct.videoId)S.yp.loadVideoById({videoId:S.ct.videoId});}
function TP(){if(!S.ct||!S.yp||!S.yr)return;S.ip?S.yp.pauseVideo():S.yp.playVideo();}
function NX(){if(!S.pl.length)return;var ni=S.pi+1;if(ni>=S.pl.length){if(S.rm==='all')ni=0;else{S.ip=false;UB();return;}}PK(S.ps,ni);}
function PV(){if(!S.pl.length)return;if(S.pt>3){if(S.yp&&S.yr)S.yp.seekTo(0);return;}var pi=S.pi-1;if(pi<0)pi=S.pl.length-1;PK(S.ps,pi);}
function SK(v){if(S.yp&&S.yr&&S.pd>0)S.yp.seekTo((parseFloat(v)/100)*S.pd,true);}
function TR(){var b=gid('btn-repeat'),d=gid('repeat-dot'),o=gid('repeat-one');if(S.rm==='off'){S.rm='all';b.classList.add('text-[#cfd3d8]');d.classList.remove('hidden');}else if(S.rm==='all'){S.rm='one';o.classList.remove('hidden');}else{S.rm='off';b.classList.remove('text-[#cfd3d8]');d.classList.add('hidden');o.classList.add('hidden');}}
function SF(){if(S.pl.length)PK(S.ps,Math.floor(Math.random()*S.pl.length));}
function toggleAutoNext(){S.autoNext=!S.autoNext;showToast(S.autoNext?'✅ Putar Berikutnya: ON':'⏸️ Putar Berikutnya: OFF');}

function shareTrack(){if(!S.ct||!S.ct.videoId)return;var url=location.origin+'/?play='+S.ct.videoId+'&share=1';updateOG(S.ct.title,S.ct.cover);if(navigator.share){navigator.share({title:S.ct.title,text:'🎵 '+S.ct.title+' - '+S.ct.artist,url:url}).catch(function(){});}}

async function FL(vid){var l=gid('lyrics-loading'),c=gid('lyrics-content'),e=gid('lyrics-empty');l.classList.remove('hidden');c.classList.add('hidden');e.classList.add('hidden');S.ld={type:'none',lines:[]};S.cli=-1;try{var r=await fetch(API.lyrics+'?id='+vid);var d=await r.json();if(d.status&&d.result.lyrics&&d.result.lyrics.lines.length>0){S.ld=d.result.lyrics;var html='';S.ld.lines.forEach(function(li,i){html+='<p class="lyric-line text-left px-2" data-time="'+li.time+'" onclick="SLT('+li.time+')" style="color:#6b7280;font-size:0.85rem;line-height:2;">'+es(li.text)+'</p>';});html+='<p class="text-center text-[#4b5563] text-xs mt-8 mb-4 opacity-50 tracking-widest">——— end ———</p>';c.innerHTML=html;l.classList.add('hidden');c.classList.remove('hidden');}else{l.classList.add('hidden');e.classList.remove('hidden');}}catch(er){l.classList.add('hidden');e.classList.remove('hidden');}}
function ULH(ct){if(S.ld.lines.length===0)return;var ls=document.querySelectorAll('.lyric-line');var ni=-1;for(var i=0;i<S.ld.lines.length;i++){if(ct>=S.ld.lines[i].time){ni=i;}}if(ni===S.cli)return;ls.forEach(function(l){l.style.color='#6b7280';l.style.fontSize='0.85rem';l.style.fontWeight='400';l.style.opacity='0.5';});ls.forEach(function(l,i){if(i<ni){l.style.color='#4b5563';l.style.opacity='0.3';}if(i===ni){l.style.color='white';l.style.fontSize='1rem';l.style.fontWeight='700';l.style.opacity='1';l.scrollIntoView({behavior:'smooth',block:'center'});}});S.cli=ni;}
function SLT(t){if(S.yp&&S.yr)S.yp.seekTo(t,true);}
function toggleLyrics(){var o=gid('lyrics-overlay');if(S.lo){o.style.transform='translateY(100%)';setTimeout(function(){o.style.display='none';},400);S.lo=false;MP.show();}else{o.style.display='flex';requestAnimationFrame(function(){requestAnimationFrame(function(){o.style.transform='translateY(0)';});});S.lo=true;MP.hide();if(S.ct&&S.ct.videoId&&S.ld.lines.length===0)FL(S.ct.videoId);}}

// PLAYLIST SYSTEM
function getUserPlaylists(){try{return JSON.parse(localStorage.getItem('nanzz_playlists')||'[]');}catch(e){return[];}}
function saveUserPlaylists(pls){try{localStorage.setItem('nanzz_playlists',JSON.stringify(pls));}catch(e){}}
function createPlaylist(name,image){var pls=getUserPlaylists();var id='pl_'+Date.now();pls.push({id:id,name:name,image:image||'',songs:[]});saveUserPlaylists(pls);return id;}
function addToPlaylistById(playlistId,track){var pls=getUserPlaylists();var pl=pls.find(function(p){return p.id===playlistId;});if(!pl)return;var exists=pl.songs.find(function(s){return s.videoId===track.videoId;});if(!exists){pl.songs.push({id:track.id,videoId:track.videoId,title:track.title,artist:track.artist,cover:track.cover,artistId:track.artistId||'',ytUrl:track.ytUrl});if(!pl.image&&pl.songs.length===1){pl.image=track.cover;}saveUserPlaylists(pls);showToast('✅ Ditambahkan ke '+pl.name);}else{showToast('⚠️ Sudah ada di playlist');}}
function showToast(msg){var toast=document.createElement('div');toast.className='fixed bottom-24 left-1/2 -translate-x-1/2 btn-chrome font-bold px-5 py-2.5 rounded-full shadow-2xl z-[999]';toast.style.animation='slideUp 0.3s ease-out forwards';toast.innerText=msg;document.body.appendChild(toast);setTimeout(function(){toast.remove();},2000);}
function addCurrentToPlaylist(){if(!S.ct)return;var pls=getUserPlaylists();if(pls.length===0){showToast('⚠️ Belum ada playlist! Buat di Library dulu');return;}showPlaylistPicker(S.ct);}
function showPlaylistPicker(track){var pls=getUserPlaylists();var popup=document.createElement('div');popup.className='fixed inset-0 z-[300] flex items-end justify-center bg-black/60';popup.onclick=function(e){if(e.target===popup)popup.remove();};var listHtml=pls.map(function(p){return'<button onclick="addToPlaylistById(\''+p.id+'\',S.ct);this.parentElement.parentElement.remove();" class="w-full text-left p-4 hover:bg-white/5 flex items-center gap-3 border-b border-white/5"><img src="'+(p.image||FI)+'" class="w-10 h-10 rounded object-cover" /><div><p class="font-medium text-white">'+p.name+'</p><p class="text-[#6b7280] text-xs">'+p.songs.length+' lagu</p></div></button>';}).join('');popup.innerHTML='<div class="glass-strong w-full max-w-md rounded-t-3xl p-6 border-t border-white/10" style="animation:slideUp 0.3s ease-out forwards;"><div class="w-10 h-1 bg-white/20 rounded-full mx-auto mb-4"></div><h3 class="font-bold text-white mb-3">Tambah ke Playlist</h3><div class="max-h-72 overflow-y-auto hide-scrollbar">'+listHtml+'</div><button onclick="this.parentElement.parentElement.remove()" class="w-full mt-3 py-3 glass glass-hover text-white rounded-full">Batal</button></div>';document.body.appendChild(popup);}