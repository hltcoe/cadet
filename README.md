CADET: Computer Assisted Discovery Extraction and Translation
============================
CADET is a system for rapid discovery, annotation, and extraction on text.
As described in the IJCNLP 2017 System Demo [paper](https://aclweb.org/anthology/I/I17/I17-3002.pdf),
CADET is a workbench for helping knowledge workers find, label, and translate documents of
	interest. It combines a multitude of analytics together with a flexible
	environment for customizing the workflow for different users. This open-source
	framework allows for easy development of new research prototypes using a
micro-service architecture based atop Docker and Apache Thrift.


cadet-broker
---------------
Library of components for the CADET broker.

cadet-tools
---------------
Development command line tools for interacting with the CADET system.

cadet-ui
---------------
Web-based user interfaces for CADET with backend Java servlets.

Using CADET
--------------
CADET requires at least an external fetch/store service and a search service.
These can be run locally or in docker containers.
See `getting_started.md` for detailed instructions on running CADET.

Citation
--------------
If you use CADET in your research, please use the following citation
```

@InProceedings{vandurme-EtAl:2017:I17-3,
  author    = {Van Durme, Benjamin  and  Lippincott, Tom  and  Duh, Kevin  and  Burchfield, Deana  and  Poliak, Adam  and  Costello, Cash  and  Finin, Tim  and  Miller, Scott  and  Mayfield, James  and  Koehn, Philipp  and  Harman, Craig  and  Lawrie, Dawn  and  May, Chandler  and  Thomas, Max  and  Carrell, Annabelle  and  Chaloux, Julianne  and  Chen, Tongfei  and  Comerford, Alex  and  Dredze, Mark  and  Glass, Benjamin  and  Hao, Shudong  and  Martin, Patrick  and  Rastogi, Pushpendre  and  Sankepally, Rashmi  and  Wolfe, Travis  and  Tran, Ying-Ying  and  Zhang, Ted},
  title     = {CADET: Computer Assisted Discovery Extraction and Translation},
  booktitle = {Proceedings of the IJCNLP 2017, System Demonstrations},
  month     = {November},
  year      = {2017},
  address   = {Tapei, Taiwan},
  publisher = {Association for Computational Linguistics},
  pages     = {5--8},
  url       = {http://www.aclweb.org/anthology/I17-3002}
}
```
