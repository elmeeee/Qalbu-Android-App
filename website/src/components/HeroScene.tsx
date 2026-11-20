import { useEffect, useMemo, useRef, type RefObject } from 'react'
import { Canvas, useFrame, useThree } from '@react-three/fiber'
import * as THREE from 'three'
import { RoomEnvironment } from 'three/examples/jsm/environments/RoomEnvironment.js'
import { RoundedBoxGeometry } from 'three/examples/jsm/geometries/RoundedBoxGeometry.js'
import type { Messages } from '../i18n/locales/id'
import { PhoneScreen, type Moment } from './PhoneScreen'

/*
 * Hero WebGL scene: a softly lit phone whose screen is real DOM (the localized
 * PhoneScreen illustration) plus a few floating ornaments derived from the
 * Sāat logo (gold eight-point stars, emerald tasbih beads).
 * Loaded lazily; the CSS phone in Hero.tsx is the static fallback.
 */

const PHONE_W = 1.5
const PHONE_H = 3.1
const PHONE_D = 0.16
const SCREEN_W = 1.38
const SCREEN_H = 2.98
const DOM_W = 360
const DOM_H = Math.round((DOM_W * SCREEN_H) / SCREEN_W)

const GOLD = '#E7B75C'
const EMERALD = '#085E43'

function roundedRectShape(w: number, h: number, r: number) {
  const s = new THREE.Shape()
  const x = -w / 2
  const y = -h / 2
  s.moveTo(x + r, y)
  s.lineTo(x + w - r, y)
  s.quadraticCurveTo(x + w, y, x + w, y + r)
  s.lineTo(x + w, y + h - r)
  s.quadraticCurveTo(x + w, y + h, x + w - r, y + h)
  s.lineTo(x + r, y + h)
  s.quadraticCurveTo(x, y + h, x, y + h - r)
  s.lineTo(x, y + r)
  s.quadraticCurveTo(x, y, x + r, y)
  return s
}

function starShape(outer: number, inner: number) {
  const s = new THREE.Shape()
  for (let i = 0; i < 16; i++) {
    const r = i % 2 === 0 ? outer : inner
    const a = (i / 16) * Math.PI * 2 + Math.PI / 8
    const px = Math.cos(a) * r
    const py = Math.sin(a) * r
    if (i === 0) s.moveTo(px, py)
    else s.lineTo(px, py)
  }
  s.closePath()
  return s
}

type Props = {
  t: Messages
  moment: Moment
  active: boolean
  onReady: () => void
  onFail: () => void
}

function Phone({ active, anchor }: { active: boolean; anchor: RefObject<THREE.Object3D | null> }) {
  const group = useRef<THREE.Group>(null)
  const pointer = useRef({ x: 0, y: 0 })
  const intro = useRef(active ? 0 : 1)
  const fine = useMemo(() => window.matchMedia('(hover: hover) and (pointer: fine)').matches, [])
  const bezel = useMemo(() => new THREE.ShapeGeometry(roundedRectShape(SCREEN_W + 0.02, SCREEN_H + 0.02, 0.15), 12), [])
  const bodyGeo = useMemo(() => new RoundedBoxGeometry(PHONE_W, PHONE_H, PHONE_D, 6, 0.2), [])
  const btnLong = useMemo(() => new RoundedBoxGeometry(0.03, 0.42, 0.06, 2, 0.012), [])
  const btnShort = useMemo(() => new RoundedBoxGeometry(0.03, 0.22, 0.06, 2, 0.012), [])

  useEffect(() => {
    if (!fine) return
    const onMove = (e: PointerEvent) => {
      pointer.current.x = (e.clientX / window.innerWidth) * 2 - 1
      pointer.current.y = (e.clientY / window.innerHeight) * 2 - 1
    }
    window.addEventListener('pointermove', onMove, { passive: true })
    return () => window.removeEventListener('pointermove', onMove)
  }, [fine])

  useFrame((state, delta) => {
    const g = group.current
    if (!g) return
    const time = state.clock.elapsedTime
    intro.current = Math.min(1, intro.current + delta / 1.4)
    const e = 1 - Math.pow(1 - intro.current, 3)
    const targetY = fine ? pointer.current.x * 0.32 - 0.22 : Math.sin(time * 0.45) * 0.16 - 0.2
    const targetX = fine ? pointer.current.y * 0.14 + 0.04 : Math.sin(time * 0.3) * 0.05 + 0.04
    const k = 1 - Math.pow(0.001, delta)
    g.rotation.y += (targetY - (1 - e) * 0.7 - g.rotation.y) * k
    g.rotation.x += (targetX - g.rotation.x) * k
    g.rotation.z = Math.sin(time * 0.35) * 0.015
    g.position.y = Math.sin(time * 0.8) * 0.05 - (1 - e) * 0.5
  })

  return (
    <group ref={group} rotation={[0.04, -0.22, 0]}>
      {/* body */}
      <mesh geometry={bodyGeo}>
        <meshPhysicalMaterial color="#0d2a20" metalness={0.75} roughness={0.28} clearcoat={1} clearcoatRoughness={0.15} />
      </mesh>
      {/* side buttons */}
      <mesh geometry={btnLong} position={[PHONE_W / 2 + 0.008, 0.7, 0]}>
        <meshStandardMaterial color="#123a2c" metalness={0.8} roughness={0.3} />
      </mesh>
      <mesh geometry={btnShort} position={[PHONE_W / 2 + 0.008, 0.2, 0]}>
        <meshStandardMaterial color="#123a2c" metalness={0.8} roughness={0.3} />
      </mesh>
      {/* glass bezel */}
      <mesh geometry={bezel} position={[0, 0, PHONE_D / 2 + 0.001]}>
        <meshStandardMaterial color="#050807" roughness={0.2} metalness={0.2} />
      </mesh>
      {/* anchor for the live DOM screen (synced via CSS 3D transforms) */}
      <object3D ref={anchor} position={[0, 0, PHONE_D / 2 + 0.003]} scale={[SCREEN_W / DOM_W, SCREEN_H / DOM_H, 1]} />
    </group>
  )
}

function Ornaments() {
  const refs = useRef<(THREE.Object3D | null)[]>([])
  const star = useMemo(() => {
    const g = new THREE.ExtrudeGeometry(starShape(0.22, 0.15), {
      depth: 0.05,
      bevelEnabled: true,
      bevelThickness: 0.02,
      bevelSize: 0.015,
      bevelSegments: 3,
    })
    g.center()
    return g
  }, [])
  const items = useMemo(
    () => [
      { kind: 'star', pos: [-1.45, 1.15, -0.4], s: 1, speed: 0.6 },
      { kind: 'star', pos: [1.4, -0.95, 0.2], s: 0.7, speed: 0.8 },
      { kind: 'star', pos: [1.25, 1.55, -0.9], s: 0.5, speed: 0.5 },
      { kind: 'bead', pos: [-1.2, -1.2, 0.3], s: 0.13, speed: 0.7 },
      { kind: 'bead', pos: [-1.0, -1.45, 0.1], s: 0.1, speed: 0.75 },
      { kind: 'bead', pos: [1.55, 0.35, -0.5], s: 0.09, speed: 0.9 },
    ],
    [],
  )
  useFrame((state) => {
    const time = state.clock.elapsedTime
    items.forEach((it, i) => {
      const o = refs.current[i]
      if (!o) return
      o.position.y = it.pos[1] + Math.sin(time * it.speed + i) * 0.08
      if (it.kind === 'star') {
        o.rotation.z = time * 0.15 * (i % 2 ? -1 : 1)
        o.rotation.y = Math.sin(time * 0.4 + i) * 0.5
      }
    })
  })
  return (
    <>
      {items.map((it, i) =>
        it.kind === 'star' ? (
          <mesh
            key={i}
            ref={(el) => {
              refs.current[i] = el
            }}
            geometry={star}
            position={it.pos as [number, number, number]}
            scale={it.s}
          >
            <meshStandardMaterial color={GOLD} metalness={0.85} roughness={0.25} />
          </mesh>
        ) : (
          <mesh
            key={i}
            ref={(el) => {
              refs.current[i] = el
            }}
            position={it.pos as [number, number, number]}
            scale={it.s}
          >
            <sphereGeometry args={[1, 32, 32]} />
            <meshPhysicalMaterial color={EMERALD} roughness={0.15} metalness={0.1} clearcoat={1} />
          </mesh>
        ),
      )}
    </>
  )
}

const cameraCSS = (m: THREE.Matrix4) => {
  const e = m.elements
  return `matrix3d(${e[0]},${-e[1]},${e[2]},${e[3]},${e[4]},${-e[5]},${e[6]},${e[7]},${e[8]},${-e[9]},${e[10]},${e[11]},${e[12]},${-e[13]},${e[14]},${e[15]})`
}
const objectCSS = (m: THREE.Matrix4) => {
  const e = m.elements
  return `matrix3d(${e[0]},${e[1]},${e[2]},${e[3]},${-e[4]},${-e[5]},${-e[6]},${-e[7]},${e[8]},${e[9]},${e[10]},${e[11]},${e[12]},${e[13]},${e[14]},${e[15]})`
}

/**
 * Keeps the DOM screen glued to the phone, using the same maths as three's
 * CSS3DRenderer. The DOM lives in the normal React tree (crisp, localized).
 */
function CssSync({
  anchor,
  view,
  cam,
  obj,
}: {
  anchor: RefObject<THREE.Object3D | null>
  view: RefObject<HTMLDivElement | null>
  cam: RefObject<HTMLDivElement | null>
  obj: RefObject<HTMLDivElement | null>
}) {
  useFrame(({ camera, size }) => {
    const a = anchor.current
    if (!a || !view.current || !cam.current || !obj.current) return
    const w = size.width / 2
    const h = size.height / 2
    const fov = camera.projectionMatrix.elements[5] * h
    camera.updateMatrixWorld()
    cam.current.style.transform = `perspective(${fov}px) translateZ(${fov}px) ${cameraCSS(camera.matrixWorldInverse)} translate(${w}px, ${h}px)`
    a.updateWorldMatrix(true, false)
    obj.current.style.transform = `translate(-50%, -50%) ${objectCSS(a.matrixWorld)}`
  })
  return null
}

function ReadySignal({ onReady, onFail }: Pick<Props, 'onReady' | 'onFail'>) {
  const gl = useThree((s) => s.gl)
  const done = useRef(false)
  useEffect(() => {
    const canvas = gl.domElement
    const lost = (e: Event) => {
      e.preventDefault()
      onFail()
    }
    canvas.addEventListener('webglcontextlost', lost)
    return () => canvas.removeEventListener('webglcontextlost', lost)
  }, [gl, onFail])
  useFrame(() => {
    if (!done.current) {
      done.current = true
      requestAnimationFrame(onReady)
    }
  })
  return null
}

/** Local, procedural environment map for soft reflections (no network fetch). */
function RoomEnv() {
  const { gl, scene } = useThree()
  useEffect(() => {
    const pmrem = new THREE.PMREMGenerator(gl)
    const env = pmrem.fromScene(new RoomEnvironment(), 0.04).texture
    scene.environment = env
    scene.environmentIntensity = 0.75
    return () => {
      scene.environment = null
      env.dispose()
      pmrem.dispose()
    }
  }, [gl, scene])
  return null
}

function Invalidator({ deps }: { deps: unknown[] }) {
  const invalidate = useThree((s) => s.invalidate)
  useEffect(() => {
    invalidate()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps)
  return null
}

export default function HeroScene({ t, moment, active, onReady, onFail }: Props) {
  const anchor = useRef<THREE.Object3D>(null)
  const view = useRef<HTMLDivElement>(null)
  const cam = useRef<HTMLDivElement>(null)
  const obj = useRef<HTMLDivElement>(null)
  return (
    <div className="hero__canvas" aria-hidden="true">
      <Canvas
        frameloop={active ? 'always' : 'demand'}
        dpr={[1, 1.75]}
        camera={{ position: [0, 0, 7.2], fov: 30 }}
        gl={{ antialias: true, alpha: true, powerPreference: 'low-power' }}
        onCreated={({ gl }) => {
          gl.setClearColor(0x000000, 0)
        }}
      >
        <ambientLight intensity={0.55} />
        <directionalLight position={[3, 4, 5]} intensity={1.5} color="#fff6e6" />
        <directionalLight position={[-4, -1, 2]} intensity={0.5} color="#bfe8d8" />
        <RoomEnv />
        <Phone active={active} anchor={anchor} />
        <Ornaments />
        <CssSync anchor={anchor} view={view} cam={cam} obj={obj} />
        <ReadySignal onReady={onReady} onFail={onFail} />
        <Invalidator deps={[moment, t]} />
      </Canvas>
      <div ref={view} className="css3d">
        <div ref={cam} className="css3d__camera">
          <div ref={obj} className="css3d__obj phone3d-screen" style={{ width: DOM_W, height: DOM_H }}>
            <PhoneScreen t={t} moment={moment} />
          </div>
        </div>
      </div>
    </div>
  )
}
