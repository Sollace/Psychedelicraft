# !/bin/bash
rm -rf ./output

target_resolutions="256 512"

echo "Target Resolution: $target_resolutions"
sleep 5

for source in item block; do
    echo "Entering ../src/main/resources/assets/psychedelicraft/textures/$source";
    for input in ../src/main/resources/assets/psychedelicraft/textures/$source/*.png; do
        for target_resolution in $target_resolutions; do
            output=${input/..\/src\/main\/resources\/assets\/psychedelicraft\/textures/.\/output\/$target_resolution};
            mkdir -p $(dirname $output);
            echo "[PNG-$target_resolution]     $input -> $output";
            ffmpeg -hide_banner -loglevel panic -i ${input} -y -frames:v 1 -vf scale=$target_resolution:-1 -sws_flags neighbor $output;
        done
    done
done
