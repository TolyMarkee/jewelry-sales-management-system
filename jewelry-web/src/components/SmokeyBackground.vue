<template>
  <div class="smokey-canvas-wrap">
    <canvas ref="canvas" />
    <div class="smokey-blur" :class="blurClass" />
  </div>
</template>

<script>
const vertexSmokeySource = `
  attribute vec4 a_position;
  void main() { gl_Position = a_position; }
`;

const fragmentSmokeySource = `
precision mediump float;
uniform vec2 iResolution;
uniform float iTime;
uniform vec2 iMouse;
uniform vec3 u_color;

void mainImage(out vec4 fragColor, in vec2 fragCoord) {
    vec2 uv = fragCoord / iResolution;
    vec2 centeredUV = (2.0 * fragCoord - iResolution.xy) / min(iResolution.x, iResolution.y);
    float time = iTime * 0.5;
    vec2 mouse = iMouse / iResolution;
    vec2 rippleCenter = 2.0 * mouse - 1.0;
    vec2 distortion = centeredUV;
    for (float i = 1.0; i < 8.0; i++) {
        distortion.x += 0.5 / i * cos(i * 2.0 * distortion.y + time + rippleCenter.x * 3.1415);
        distortion.y += 0.5 / i * cos(i * 2.0 * distortion.x + time + rippleCenter.y * 3.1415);
    }
    float wave = abs(sin(distortion.x + distortion.y + time));
    float glow = smoothstep(0.9, 0.2, wave);
    fragColor = vec4(u_color * glow, 1.0);
}

void main() {
    mainImage(gl_FragColor, gl_FragCoord.xy);
}
`;

const blurMap = { none: '', sm: 'blur-sm', md: 'blur-md', lg: 'blur-lg', xl: 'blur-xl', '2xl': 'blur-2xl', '3xl': 'blur-3xl' };

export default {
  name: 'SmokeyBackground',
  props: {
    blur: { type: String, default: 'sm' },
    color: { type: String, default: '#1E40AF' }
  },
  data() {
    return {
      mouse: { x: 0, y: 0 },
      hovering: false,
      animId: null
    }
  },
  computed: {
    blurClass() { return blurMap[this.blur] || blurMap['sm']; }
  },
  mounted() { this.initWebGL(); },
  beforeDestroy() {
    if (this.animId) cancelAnimationFrame(this.animId);
    const canvas = this.$refs.canvas;
    if (canvas) {
      canvas.removeEventListener('mousemove', this._handleMouseMove);
      canvas.removeEventListener('mouseenter', this._handleMouseEnter);
      canvas.removeEventListener('mouseleave', this._handleMouseLeave);
    }
  },
  methods: {
    initWebGL() {
      const canvas = this.$refs.canvas;
      if (!canvas) return;
      const gl = canvas.getContext('webgl');
      if (!gl) { console.error('WebGL not supported'); return; }

      const compileShader = (type, source) => {
        const shader = gl.createShader(type);
        gl.shaderSource(shader, source);
        gl.compileShader(shader);
        if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) {
          console.error('Shader error:', gl.getShaderInfoLog(shader));
          return null;
        }
        return shader;
      };

      const vs = compileShader(gl.VERTEX_SHADER, vertexSmokeySource);
      const fs = compileShader(gl.FRAGMENT_SHADER, fragmentSmokeySource);
      if (!vs || !fs) return;

      const program = gl.createProgram();
      gl.attachShader(program, vs);
      gl.attachShader(program, fs);
      gl.linkProgram(program);
      if (!gl.getProgramParameter(program, gl.LINK_STATUS)) {
        console.error('Link error:', gl.getProgramInfoLog(program));
        return;
      }
      gl.useProgram(program);

      const buf = gl.createBuffer();
      gl.bindBuffer(gl.ARRAY_BUFFER, buf);
      gl.bufferData(gl.ARRAY_BUFFER, new Float32Array([-1,-1, 1,-1, -1,1, -1,1, 1,-1, 1,1]), gl.STATIC_DRAW);
      const posLoc = gl.getAttribLocation(program, 'a_position');
      gl.enableVertexAttribArray(posLoc);
      gl.vertexAttribPointer(posLoc, 2, gl.FLOAT, false, 0, 0);

      const resLoc = gl.getUniformLocation(program, 'iResolution');
      const timeLoc = gl.getUniformLocation(program, 'iTime');
      const mouseLoc = gl.getUniformLocation(program, 'iMouse');
      const colorLoc = gl.getUniformLocation(program, 'u_color');

      const hexToRgb = (hex) => [
        parseInt(hex.substring(1,3),16)/255,
        parseInt(hex.substring(3,5),16)/255,
        parseInt(hex.substring(5,7),16)/255
      ];
      const [r,g,b] = hexToRgb(this.color);
      gl.uniform3f(colorLoc, r, g, b);

      const startTime = Date.now();
      const self = this;

      const render = () => {
        const w = canvas.clientWidth;
        const h = canvas.clientHeight;
        canvas.width = w; canvas.height = h;
        gl.viewport(0, 0, w, h);

        gl.uniform2f(resLoc, w, h);
        gl.uniform1f(timeLoc, (Date.now() - startTime) / 1000);
        gl.uniform2f(mouseLoc,
          self.hovering ? self.mouse.x : w/2,
          self.hovering ? h - self.mouse.y : h/2
        );

        gl.drawArrays(gl.TRIANGLES, 0, 6);
        self.animId = requestAnimationFrame(render);
      };

      this._handleMouseMove = (e) => {
        const rect = canvas.getBoundingClientRect();
        this.mouse = { x: e.clientX - rect.left, y: e.clientY - rect.top };
      };
      this._handleMouseEnter = () => { this.hovering = true; };
      this._handleMouseLeave = () => { this.hovering = false; };

      canvas.addEventListener('mousemove', this._handleMouseMove);
      canvas.addEventListener('mouseenter', this._handleMouseEnter);
      canvas.addEventListener('mouseleave', this._handleMouseLeave);

      render();
    }
  }
};
</script>

<style scoped>
.smokey-canvas-wrap { position: absolute; inset: 0; overflow: hidden; }
.smokey-canvas-wrap canvas { width: 100%; height: 100%; }
.smokey-blur { position: absolute; inset: 0; }
.blur-sm { backdrop-filter: blur(4px); background: rgba(0,0,0,0.15); }
.blur-md { backdrop-filter: blur(8px); background: rgba(0,0,0,0.2); }
.blur-lg { backdrop-filter: blur(12px); background: rgba(0,0,0,0.25); }
</style>
