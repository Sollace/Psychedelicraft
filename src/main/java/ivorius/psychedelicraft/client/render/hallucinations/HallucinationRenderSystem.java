package ivorius.psychedelicraft.client.render.hallucinations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ivorius.psychedelicraft.entity.drug.hallucination.Hallucination;
import ivorius.psychedelicraft.entity.drug.hallucination.HallucinationManager;
import ivorius.psychedelicraft.entity.drug.hallucination.HallucinationTypeKeys;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.util.Identifier;

public class HallucinationRenderSystem {
    public static final HallucinationRenderSystem INSTANCE = new HallucinationRenderSystem();

    private final Map<Identifier, HallucinationRenderer<?, ?>> renderers = new HashMap<>();

    public HallucinationRenderSystem() {
        renderers.put(HallucinationTypeKeys.MULTIPLE_ENTITY, new MultipleEntityHallucinationRenderer());
        renderers.put(HallucinationTypeKeys.RASTA_HEAD, new RastaHeadHallucinationRenderer());
        renderers.put(HallucinationTypeKeys.SINGLE_ENTITY, new AbstractEntityHallucinationRenderer<>() {
            @Override
            public AbstractEntityHallucinationRenderer.State createRenderState() {
                return new AbstractEntityHallucinationRenderer.State();
            }
        });
    }

    public void register(Identifier id, HallucinationRenderer<?, ?> renderer) {
        renderers.put(id, renderer);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <H extends Hallucination> HallucinationRenderer<H, ?> getRenderer(H hallucination) {
        return (HallucinationRenderer)renderers.get(hallucination.getType());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <S extends HallucinationRenderState> HallucinationRenderer<?, S> getRenderer(S state) {
        return (HallucinationRenderer)renderers.get(Objects.requireNonNull(state.type, "State does not have a type"));
    }

    public List<HallucinationRenderState> extractStates(Frustum frustum, Camera camera, HallucinationManager hallucinations, WorldRenderState worldState, float tickDelta) {
        List<HallucinationRenderState> states = new ArrayList<>();

        float alpha = hallucinations.getEntityHallucinationAlphaTransparency(tickDelta);

        for (Hallucination h : hallucinations.getEntities()) {
            HallucinationRenderer<Hallucination, ?> renderer = getRenderer(h);
            if (renderer != null && renderer.shouldRender(h, frustum, camera.getCameraPos(), alpha, tickDelta)) {
                states.add(renderer.createRenderState(h, tickDelta, alpha, worldState.cameraRenderState));
            }
        }

        return states;
    }
}
