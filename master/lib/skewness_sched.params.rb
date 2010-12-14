# this is actually a ruby file

$pm_capacity = {
  ["10.0.1.206", "femi"] => {
    "hw_cpu_MHz" => 4000,
    "hw_ram_MB" => 8000,
    "hw_link_Mbps" => 1000,
    "hw_num_nics" => 1,
    "hw_num_cpus" => 1
  },
  "femi3" => {
    "hw_cpu_MHz" => 4000,
    "hw_ram_MB" => 8000,
    "hw_link_Mbps" => 1000,
    "hw_num_nics" => 1,
    "hw_num_cpus" => 1
  },
# default capacity
  ".default" => {
    "hw_cpu_MHz" => 4000,
    "hw_ram_MB" => 8000,
    "hw_link_Mbps" => 1000,
    "hw_num_nics" => 1,
    "hw_num_cpus" => 1
  }
}

$script_body =<<EOF
# This is the body of the config.py

#vm_load_predictor = "SingleEWMAPredictor"
#vm_load_predictor_param = {'alpha':0.1, 'pspan':1, 'slath':0.1}
#vm_load_predictor_param = {'alpha':0.0, 'pspan':1, 'slath':0.9}
#pm_load_predictor = "SingleEWMAPredictor"
#pm_load_predictor_param = {'alpha':0.1, 'pspan':1, 'slath':0.1}
#pm_load_predictor_param = {'alpha':0.0, 'pspan':1, 'slath':0.9}
#sys_load_predictor = "SingleEWMAPredictor"
#sys_load_predictor_param = {'alpha':0.1, 'pspan':1, 'slath':0.1}
#sys_load_predictor_param = {'alpha':0.0, 'pspan':1, 'slath':0.9}
vm_load_predictor = "FastUpSlowDownPredictor"
vm_load_predictor_param = {'upalpha':-0.2, 'downalpha':0.7, 'pspan':8, 'slath':0.9}
pm_load_predictor = "FastUpSlowDownPredictor"
pm_load_predictor_param = {'upalpha':-0.2, 'downalpha':0.7, 'pspan':8, 'slath':0.9}
sys_load_predictor = "FastUpSlowDownPredictor"
sys_load_predictor_param = {'upalpha':-0.2, 'downalpha':0.7, 'pspan':8, 'slath':0.9}

# heuristic config
# name
heuristic_name = "skewness"
# args
hot_threshold = 0.9
heuristic_args = {
    "skewness": {
        "hot": 0.9,
        "warm": 0.65,
        "cold": 0.25,
        "max_mig_num": 2,
        "greencomputing_threshold": 0.4,
        "coldlimit": 0.05
    },
    "astar": {
        "hot": hot_threshold
    },
    "theory":{
	"hot": hot_threshold,
	"cold": 0.25,
	"warm": 1.0,
	"greencomputing_threshold": 0.45,
	"dispersedness": 20.0,
    },
    "balance": {
        "hot": hot_threshold,
        "warm": 0.65
    },
    "distance": {
        "hot": hot_threshold,
        "warm": 0.65
    },
    "blackbox": {
        "max_swap": 10000,
        "max_cpu": hot_threshold,
        "max_net": hot_threshold,
        "hot": hot_threshold
    }
}
# set initial scheduler according to name
heuristic = __import__("heuristic.%s" % heuristic_name
        , globals(), locals(), ['*'])
algorithm_args = heuristic_args[heuristic_name]
algorithm = heuristic.Algorithm

EOF

