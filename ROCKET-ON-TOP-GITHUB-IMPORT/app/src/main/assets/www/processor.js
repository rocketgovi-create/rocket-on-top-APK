class KaizerProcessor extends AudioWorkletProcessor {
  static get parameterDescriptors(){return [
    {name:'micGain',defaultValue:1,minValue:0,maxValue:5},{name:'loudness',defaultValue:1,minValue:0,maxValue:5},
    {name:'bass',defaultValue:1,minValue:0,maxValue:5},{name:'mid',defaultValue:1,minValue:0,maxValue:5},{name:'treble',defaultValue:1,minValue:0,maxValue:5},
    {name:'echo',defaultValue:0,minValue:0,maxValue:1},{name:'reverb',defaultValue:0,minValue:0,maxValue:1},{name:'noise',defaultValue:0,minValue:0,maxValue:1},
    {name:'gate',defaultValue:.001,minValue:0,maxValue:1},{name:'drive',defaultValue:0,minValue:0,maxValue:1},{name:'clipProtection',defaultValue:1,minValue:0,maxValue:1}
  ];}
  constructor(){super();this.env=0;this.seed=123456789;this.lpL=0;this.lpR=0;this.prevL=0;this.prevR=0;this.echoBuf=new Float32Array(24000);this.echoPos=0;this.rev=new Float32Array(16000);this.rp=0;}
  rnd(){this.seed=(this.seed*1664525+1013904223)>>>0;return(this.seed/4294967295)*2-1;}
  process(inputs,outputs,p){const input=inputs[0],output=outputs[0];if(!input?.[0]||!output?.[0])return true;const a=input[0],b=input[1]||input[0],oL=output[0],oR=output[1]||output[0];const v=n=>p[n][0];const mic=v('micGain'),master=v('loudness'),bass=v('bass'),mid=v('mid'),treble=v('treble'),echo=v('echo'),reverb=v('reverb'),noise=v('noise'),gate=v('gate'),drive=v('drive'),protect=v('clipProtection');let peak=0;
    for(let i=0;i<a.length;i++){let L=a[i],R=b[i];const level=Math.abs((L+R)*.5);if(level>gate)this.env=Math.min(1,this.env+.045);else this.env=Math.max(0,this.env-.0035);L*=this.env*mic;R*=this.env*mic;
      this.lpL=this.lpL*.985+L*.015;this.lpR=this.lpR*.985+R*.015;const hiL=L-this.prevL*.92,hiR=R-this.prevR*.92;this.prevL=L;this.prevR=R;
      L=this.lpL*.55*bass+L*.70*mid+hiL*.62*treble;R=this.lpR*.55*bass+R*.70*mid+hiR*.62*treble;
      if(noise>.001){const n=noise*.025*Math.max(this.env,.15);L+=this.rnd()*n;R+=this.rnd()*n;}
      if(reverb>.001){const d=this.rev[this.rp],d2=this.rev[(this.rp+7200)%this.rev.length];this.rev[this.rp]=(L+R)*.28+d*.22;this.rp=(this.rp+1)%this.rev.length;L+=d*reverb*.45;R+=d2*reverb*.45;}
      if(echo>.001){const d=this.echoBuf[this.echoPos];this.echoBuf[this.echoPos]=(L+R)*.5+d*.38;this.echoPos=(this.echoPos+1)%this.echoBuf.length;L+=d*echo*.65;R+=d*echo*.65;}
      const pre=L*master,preR=R*master;const saturation=1+drive*8;L=Math.tanh(pre*saturation);R=Math.tanh(preR*saturation);
      // Hard true-peak safety ceiling remains active even if optional clipping coloration is disabled.
      const ceiling=protect>.5?.89125:.98;L=Math.max(-ceiling,Math.min(ceiling,L));R=Math.max(-ceiling,Math.min(ceiling,R));
      oL[i]=L;if(output[1])oR[i]=R;if((i&31)===0)peak=Math.max(peak,Math.abs(L),Math.abs(R));
    }if(currentFrame%8===0)this.port.postMessage({peak});return true;}
}
registerProcessor('kaizer-processor',KaizerProcessor);
