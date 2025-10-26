package dev.micalobia.fullslabs.client;

import dev.micalobia.fullslabs.util.Constants;
import dev.micalobia.fullslabs.util.SlabPlacement;
import dev.micalobia.fullslabs.util.Utility;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class BlockFaceOverlay {
    private static final double EPSILON = 1e-4d;
    private static final int FILL_COLOR = 0x3F007FFF;
    private static final int LINE_COLOR = 0xFFFFFFFF;

    private static final RenderLayer QUAD_LAYER = RenderLayer.getDebugQuads();
    private static final RenderLayer LINE_LAYER = RenderLayer.getDebugLineStrip(2f);

    private BlockFaceOverlay() {
    }

    public static void renderFaceOverlay(WorldRenderState renderState) {
        var mc = MinecraftClient.getInstance();
        if (!(mc.crosshairTarget instanceof BlockHitResult bhr)) return;
        var player = mc.player;
        if (player == null) return;
        if (!player.isHolding(Utility::isSlabWithVertical)) return;
        var world = mc.world;
        if (world == null) return;
        var pos = bhr.getBlockPos();
        var face = bhr.getSide();
        var hit = bhr.getPos();
        var state = world.getBlockState(pos);
        renderFaceOverlay(player, renderState.cameraRenderState.pos, world, pos, state, face, hit);
    }

    private static void renderFaceOverlay(PlayerEntity player, Vec3d camera, BlockRenderView world, BlockPos pos, BlockState state, Direction face, Vec3d hit) {
        final var frame = FaceFrame.create(face);
        final var playerFacing = player.getHorizontalFacing();
        final var at = Utility.isSlab(state) && Utility.isInsideSlab(state, pos, hit) ? null : getRegion(face, playerFacing, pos, hit);
        final var outline = state.getOutlineShape(world, pos, ShapeContext.of(player));
        if (outline.isEmpty()) return;
        var nHit = hit.subtract(pos.getX(), pos.getY(), pos.getZ());
        var map = new LinkedHashMap<RenderLayer, BufferAllocator>();
        map.put(QUAD_LAYER, new BufferAllocator(1024));
        map.put(LINE_LAYER, new BufferAllocator(512));
        var immediate = VertexConsumerProvider.immediate(map, new BufferAllocator(1024));
        var stack = new MatrixStack();
        stack.push();
        stack.translate(pos.getX() + 0.5d - camera.x, pos.getY() + 0.5d - camera.y, pos.getZ() + 0.5d - camera.z);
        switch (face) {
            case DOWN -> stack.translate(0, nHit.y, 0);
            case UP -> stack.translate(0, nHit.y - 1, 0);
            case NORTH -> stack.translate(0, 0, nHit.z);
            case SOUTH -> stack.translate(0, 0, nHit.z - 1);
            case WEST -> stack.translate(nHit.x, 0, 0);
            case EAST -> stack.translate(nHit.x - 1, 0, 0);
        }
        var entry = stack.peek();
        var edgeMap = new HashMap<EdgeKey, UVSeg>();
        outline.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
            var rect = faceRectOnBox(face, minX, minY, minZ, maxX, maxY, maxZ, nHit.x, nHit.y, nHit.z);
            if (rect == null || rect.isDegenerate()) return;
            var poly = rectToCenteredPolygon(rect);
            poly = clipPolygonByAt(poly, at);
            var size = poly.size();
            for (var i = 0; i < size; ++i) {
                var a = poly.get(i);
                var b = poly.get((i + 1) % size);
                addEdgeQuantized(edgeMap, a, b);
            }
            emitFill(entry, immediate, frame, poly);
        });
        immediate.draw();
        drawLines(entry, immediate, frame, edgeMap);
        stack.pop();
    }

    private static void putVertex(VertexConsumer vc, MatrixStack.Entry e, Vec3d p, Vec3d n) {
        vc.vertex(e.getPositionMatrix(), (float) p.x, (float) p.y, (float) p.z).color(FILL_COLOR).normal(e, (float) n.x, (float) n.y, (float) n.z);
    }

    private static Vec3d uvToWorld(double u, double v, FaceFrame b) {
        var half = 0.5d;
        var px = u - half;
        var py = v - half;

        Vec3d U = b.u(), V = b.v(), N = b.n();
        var x = U.getX() * px + V.getX() * py + N.getX() * half;
        var y = U.getY() * px + V.getY() * py + N.getY() * half;
        var z = U.getZ() * px + V.getZ() * py + N.getZ() * half;
        return new Vec3d(x, y, z);
    }

    @SuppressWarnings("DuplicatedCode") // Silly that this even happens, btw
    private static RectUV faceRectOnBox(Direction face, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, double depthX, double depthY, double depthZ) {
        final var eps = 1e-4;

        return switch (face) {
            case NORTH -> Math.abs(minZ - depthZ) < eps ? new RectUV(minX, minY, maxX, maxY) : null;
            case SOUTH -> Math.abs(maxZ - depthZ) < eps ? new RectUV(minX, minY, maxX, maxY) : null;
            case WEST -> Math.abs(minX - depthX) < eps ? new RectUV(minZ, minY, maxZ, maxY) : null;
            case EAST -> Math.abs(maxX - depthX) < eps ? new RectUV(minZ, minY, maxZ, maxY) : null;
            case DOWN -> Math.abs(minY - depthY) < eps ? new RectUV(minX, minZ, maxX, maxZ) : null;
            case UP -> Math.abs(maxY - depthY) < eps ? new RectUV(minX, minZ, maxX, maxZ) : null;
        };
    }

    private static List<Vec2f> rectToCenteredPolygon(RectUV r) {
        float u0 = (float) (r.u0 - 0.5d), v0 = (float) (r.v0 - 0.5d);
        float u1 = (float) (r.u1 - 0.5d), v1 = (float) (r.v1 - 0.5d);
        List<Vec2f> poly = new ArrayList<>(4);
        poly.add(new Vec2f(u0, v0));
        poly.add(new Vec2f(u1, v0));
        poly.add(new Vec2f(u1, v1));
        poly.add(new Vec2f(u0, v1));
        return poly;
    }

    private static List<Vec2f> clipHalfPlane(List<Vec2f> in, float a, float b, float d) {
        if (in.isEmpty()) return in;
        List<Vec2f> out = new ArrayList<>(in.size() + 4);
        var prev = in.getLast();
        var prevL = a * prev.x + b * prev.y + d;
        var prevIn = prevL <= 0f + 1e-6f;

        for (var curr : in) {
            var currL = a * curr.x + b * curr.y + d;
            var currIn = currL <= 0f + 1e-6f;

            if (currIn != prevIn) {
                var denom = a * (curr.x - prev.x) + b * (curr.y - prev.y);
                if (Math.abs(denom) > 1e-7f) {
                    var t = -prevL / denom;
                    var ix = prev.x + t * (curr.x - prev.x);
                    var iy = prev.y + t * (curr.y - prev.y);
                    out.add(new Vec2f(ix, iy));
                }
            }
            if (currIn) out.add(curr);

            prev = curr;
            prevL = currL;
            prevIn = currIn;
        }
        return out;
    }

    private static List<Vec2f> clipPolygonByAt(List<Vec2f> poly, @Nullable BlockFaceOverlay.FaceRegion at) {
        final var inner = (float) Constants.EDGE_WIDTH;

        return switch (at) {
            case CENTER -> {
                poly = clipHalfPlane(poly, 1, 0, -inner);
                poly = clipHalfPlane(poly, -1, 0, -inner);
                poly = clipHalfPlane(poly, 0, 1, -inner);
                poly = clipHalfPlane(poly, 0, -1, -inner);
                yield poly;
            }
            case LEFT -> {
                poly = clipHalfPlane(poly, 1, 0, inner);
                poly = clipHalfPlane(poly, 1, -1, 0);
                poly = clipHalfPlane(poly, 1, 1, 0);
                yield poly;
            }
            case RIGHT -> {
                poly = clipHalfPlane(poly, -1, 0, inner);
                poly = clipHalfPlane(poly, -1, 1, 0);
                poly = clipHalfPlane(poly, -1, -1, 0);
                yield poly;
            }
            case TOP -> {
                poly = clipHalfPlane(poly, 0, -1, inner);
                poly = clipHalfPlane(poly, 1, -1, 0);
                poly = clipHalfPlane(poly, -1, -1, 0);
                yield poly;
            }
            case BOTTOM -> {
                poly = clipHalfPlane(poly, 0, 1, inner);
                poly = clipHalfPlane(poly, -1, 1, 0);
                poly = clipHalfPlane(poly, 1, 1, 0);
                yield poly;
            }
            case null -> poly;
        };
    }

    private static void emitFill(MatrixStack.Entry entry, VertexConsumerProvider provider, FaceFrame frame, List<Vec2f> centeredPoly) {
        if (centeredPoly.size() < 3) return;
        var n = frame.n();
        var size = centeredPoly.size();
        var vc = provider.getBuffer(QUAD_LAYER);
        var p0 = uvToWorld(centeredPoly.getFirst().x + 0.5d, centeredPoly.getFirst().y + 0.5d, frame).add(n.getX() * EPSILON, n.getY() * EPSILON, n.getZ() * EPSILON);
        for (var i = 1; i < size - 1; ++i) {
            var p1 = uvToWorld(centeredPoly.get(i).x + 0.5d, centeredPoly.get(i).y + 0.5d, frame).add(n.getX() * EPSILON, n.getY() * EPSILON, n.getZ() * EPSILON);
            var p2 = uvToWorld(centeredPoly.get(i + 1).x + 0.5d, centeredPoly.get(i + 1).y + 0.5d, frame).add(n.getX() * EPSILON, n.getY() * EPSILON, n.getZ() * EPSILON);
            putVertex(vc, entry, p0, n);
            putVertex(vc, entry, p1, n);
            putVertex(vc, entry, p2, n);
            putVertex(vc, entry, p2, n);
        }
    }

    private static void drawLines(MatrixStack.Entry entry, VertexConsumerProvider.Immediate provider, FaceFrame frame, Map<EdgeKey, UVSeg> edgeMap) {
        var n = frame.n();
        var offset = n.multiply(EPSILON);
        var chains = buildChains(edgeMap);
        for (var chain : chains) {
            chain = mergeColinear(chain);
            var vc = provider.getBuffer(LINE_LAYER);
            for (var v : chain) {
                var p = uvToWorld(v.x + 0.5d, v.y + 0.5d, frame).add(offset);
                vc.vertex(entry, (float) p.x, (float) p.y, (float) p.z).color(LINE_COLOR).normal(entry, (float) n.x, (float) n.y, (float) n.z);
            }
            provider.draw();
        }
    }

    private static List<Vec2f> mergeColinear(List<Vec2f> in) {
        if (in.size() < 2) return in;
        var out = new ArrayList<Vec2f>(in.size());
        var prev = in.getFirst();
        out.add(prev);

        int pdx = Integer.MIN_VALUE, pdy = Integer.MIN_VALUE;
        for (int i = 1; i < in.size(); i++) {
            var curr = in.get(i);
            int dx = Integer.signum(Math.round((curr.x - prev.x) * EdgeKey.EDGE_Q));
            int dy = Integer.signum(Math.round((curr.y - prev.y) * EdgeKey.EDGE_Q));
            if (dx == 0 && dy == 0) continue;

            if (pdx == Integer.MIN_VALUE) {
                pdx = dx;
                pdy = dy;
                out.add(curr);
            } else if (dx == pdx && dy == pdy) {
                out.set(out.size() - 1, curr);
            } else {
                pdx = dx;
                pdy = dy;
                out.add(curr);
            }
            prev = curr;
        }
        return out;
    }

    private static FaceRegion getRegion(Direction face, Direction playerFacing, BlockPos pos, Vec3d hit) {
        var targeted = SlabPlacement.getTargetedDirection(face, playerFacing, pos, hit);
        if (targeted == face.getOpposite()) return FaceRegion.CENTER;
        if (targeted == Direction.UP) return FaceRegion.TOP;
        if (targeted == Direction.DOWN) return FaceRegion.BOTTOM;
        return switch (face) {
            case NORTH, SOUTH -> switch (targeted) {
                case EAST -> FaceRegion.RIGHT;
                case WEST -> FaceRegion.LEFT;
                default -> FaceRegion.CENTER;
            };
            case EAST, WEST -> switch (targeted) {
                case NORTH -> FaceRegion.LEFT;
                case SOUTH -> FaceRegion.RIGHT;
                default -> FaceRegion.CENTER;
            };
            case UP, DOWN -> switch (targeted) {
                case EAST -> FaceRegion.RIGHT;
                case WEST -> FaceRegion.LEFT;
                case NORTH -> FaceRegion.BOTTOM;
                case SOUTH -> FaceRegion.TOP;
                default -> FaceRegion.CENTER;
            };
        };
    }

    private static void addEdgeQuantized(Map<EdgeKey, UVSeg> map, Vec2f a, Vec2f b) {
        var ax = EdgeKey.quantize(a.x);
        var ay = EdgeKey.quantize(a.y);
        var bx = EdgeKey.quantize(b.x);
        var by = EdgeKey.quantize(b.y);
        var dx = Integer.signum(bx - ax);
        var dy = Integer.signum(by - ay);
        var steps = Math.max(Math.abs(bx - ax), Math.abs(by - ay));
        if (steps == 0) return;
        var x = ax;
        var y = ay;
        for (var i = 0; i < steps; ++i) {
            var nx = x + dx;
            var ny = y + dy;
            var ek = new EdgeKey(x, y, nx, ny);
            var seg = map.get(ek);
            if (seg == null)
                map.put(ek, new UVSeg(new Vec2f((float) x / EdgeKey.EDGE_Q, (float) y / EdgeKey.EDGE_Q), new Vec2f((float) nx / EdgeKey.EDGE_Q, (float) ny / EdgeKey.EDGE_Q)));
            else ++seg.count;
            x = nx;
            y = ny;
        }
    }

    private static List<List<Vec2f>> buildChains(Map<EdgeKey, UVSeg> edgeMap) {
        var unitEdges = edgeMap.values().stream().filter(e -> e.count == 1).toList();

        var adjacent = new HashMap<QuantizedUV, List<QuantizedUV>>();
        for (var e : unitEdges) {
            var a = QuantizedUV.fromVec2f(e.a);
            var b = QuantizedUV.fromVec2f(e.b);
            adjacent.computeIfAbsent(a, k -> new ArrayList<>()).add(b);
            adjacent.computeIfAbsent(b, k -> new ArrayList<>()).add(a);
        }

        var unused = new HashSet<Long>();
        for (var e : unitEdges) {
            unused.add(edgeKey(QuantizedUV.fromVec2f(e.a), QuantizedUV.fromVec2f(e.b)));
        }

        var chains = new ArrayList<List<Vec2f>>();

        for (var start : adjacent.keySet()) {
            if (adjacent.get(start).size() == 2) continue;
            var chainQ = walkChainFrom(start, adjacent, unused);
            if (chainQ.size() >= 2) chains.add(toVec2f(chainQ));
        }

        while (!unused.isEmpty()) {
            QuantizedUV seed = null;
            outer:
            for (var n : adjacent.keySet()) {
                for (var m : adjacent.getOrDefault(n, List.of())) {
                    if (unused.contains(edgeKey(n, m))) {
                        seed = n;
                        break outer;
                    }
                }
            }
            if (seed == null) break;
            var loopQ = walkChainFrom(seed, adjacent, unused);
            if (loopQ.size() >= 3) {
                loopQ.add(loopQ.getFirst());
                chains.add(toVec2f(loopQ));
            }
        }

        return chains;
    }

    private static List<QuantizedUV> walkChainFrom(QuantizedUV start, Map<QuantizedUV, List<QuantizedUV>> adjacent, Set<Long> unused) {
        var chain = new ArrayList<QuantizedUV>(64);
        QuantizedUV prev = null, curr = start;
        chain.add(curr);

        while (true) {
            QuantizedUV next = null;
            for (var cand : adjacent.getOrDefault(curr, List.of())) {
                if (cand.equals(prev)) continue;
                var ek = edgeKey(curr, cand);
                if (unused.remove(ek)) {
                    next = cand;
                    break;
                }
            }
            if (next == null) break;
            chain.add(next);
            prev = curr;
            curr = next;
        }
        return chain;
    }

    private static List<Vec2f> toVec2f(List<QuantizedUV> nodes) {
        var out = new ArrayList<Vec2f>(nodes.size());
        for (var q : nodes) out.add(new Vec2f(q.x / (float) EdgeKey.EDGE_Q, q.y / (float) EdgeKey.EDGE_Q));
        return out;
    }

    private static long edgeKey(QuantizedUV a, QuantizedUV b) {
        if (a.x > b.x || a.x == b.x && a.y > b.y) {
            var t = a;
            a = b;
            b = t;
        }
        return (long) a.x << 48 ^ (long) a.y << 32 ^ (long) b.x << 16 ^ (long) b.y;
    }

    private record RectUV(double u0, double v0, double u1, double v1) {
        private RectUV(double u0, double v0, double u1, double v1) {
            if (u0 <= u1) {
                this.u0 = u0;
                this.u1 = u1;
            } else {
                this.u0 = u1;
                this.u1 = u0;
            }
            if (v0 <= v1) {
                this.v0 = v0;
                this.v1 = v1;
            } else {
                this.v0 = v1;
                this.v1 = v0;
            }
        }

        boolean isDegenerate() {
            return u1 - u0 <= 1e-6 || v1 - v0 <= 1e-6;
        }
    }

    private static final class EdgeKey {
        public static final int EDGE_Q = 1024;
        final int ax, ay, bx, by;

        public EdgeKey(float ax, float ay, float bx, float by) {
            var qax = quantize(ax);
            var qay = quantize(ay);
            var qbx = quantize(bx);
            var qby = quantize(by);
            if (qax < qbx || qax == qbx && qay <= qby) {
                this.ax = qax;
                this.ay = qay;
                this.bx = qbx;
                this.by = qby;
            } else {
                this.ax = qbx;
                this.ay = qby;
                this.bx = qax;
                this.by = qay;
            }
        }

        public static int quantize(float v) {
            return Math.round(v * EDGE_Q);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof EdgeKey e)) return false;
            return ax == e.ax && ay == e.ay && bx == e.bx && by == e.by;
        }

        @Override
        public int hashCode() {
            var h = ax;
            h = 31 * h + ay;
            h = 31 * h + bx;
            h = 31 * h + by;
            return h;
        }

    }

    private static final class UVSeg {
        final Vec2f a, b;
        int count = 1;

        UVSeg(Vec2f a, Vec2f b) {
            this.a = a;
            this.b = b;
        }
    }

    // The names of these values isn't as meaningful as you might think
    private enum FaceRegion {
        CENTER,
        LEFT,
        RIGHT,
        BOTTOM,
        TOP
    }

    public record FaceFrame(Direction face, Vec3d u, Vec3d v, Vec3d n) {
        public static FaceFrame create(Direction face) {
            return switch (face) {
                case NORTH -> new FaceFrame(face, new Vec3d(1, 0, 0), new Vec3d(0, 1, 0), new Vec3d(0, 0, -1));
                case SOUTH -> new FaceFrame(face, new Vec3d(1, 0, 0), new Vec3d(0, 1, 0), new Vec3d(0, 0, 1));
                case WEST -> new FaceFrame(face, new Vec3d(0, 0, 1), new Vec3d(0, 1, 0), new Vec3d(-1, 0, 0));
                case EAST -> new FaceFrame(face, new Vec3d(0, 0, 1), new Vec3d(0, 1, 0), new Vec3d(1, 0, 0));
                case DOWN -> new FaceFrame(face, new Vec3d(1, 0, 0), new Vec3d(0, 0, 1), new Vec3d(0, -1, 0));
                case UP -> new FaceFrame(face, new Vec3d(1, 0, 0), new Vec3d(0, 0, 1), new Vec3d(0, 1, 0));
            };
        }
    }

    private record QuantizedUV(int x, int y) {
        public static QuantizedUV fromVec2f(Vec2f v) {
            return new QuantizedUV(EdgeKey.quantize(v.x), EdgeKey.quantize(v.y));
        }
    }
}
